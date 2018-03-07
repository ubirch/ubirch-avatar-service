package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.{Config, Const}
import com.ubirch.avatar.core.device.{DeviceManager, DeviceTypeManager}
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.Future
import scala.io.Source

object ImportProdLogs
  extends App
    with StrictLogging {

  implicit val system = ActorSystem("AvatarService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  case class DeviceInfo(
                         deviceType: String,
                         testResult: Boolean,
                         hwDeviceId: String
                       )

  val basePath = "/Volumes/GoogleDrive/My Drive/trackle/Dokumentation/TD Dokumente/Elsa/TD-In Bearbeitung/Produktvalidierung/Software Tests/Produktion"

  val prodLogs = Set[String](
    "20180301-production-log.csv"
  )

  val defaultGroup = UUIDUtil.uuid

  val sep = "\t"
  val envId = "local_dev"

  val rawQueue = s"$envId-trackle-service-inbox"

  val deviceTypeOffset = 0
  val testResultOffset = 5
  val hwDeviceIdOffset = 7

  prodLogs.foreach { fn =>
    val deviceRows = Source.fromFile(s"$basePath/$fn").getLines().toList.tail
    deviceRows.foreach { row =>
      val rowData = row.split(sep)

      val di = DeviceInfo(
        deviceType = rowData(deviceTypeOffset).toString,
        testResult = !rowData(testResultOffset).toString.toLowerCase.contains("failed"),
        hwDeviceId = rowData(hwDeviceIdOffset).toString
      )
      logger.info(s"deviceInfo: ${di.deviceType} / ${di.testResult} / ${di.hwDeviceId}")
      getDeviceTypeKey(di.deviceType).map { dtype =>
        DeviceManager.infoByHwId(di.hwDeviceId).map {
          case Some(dev) =>
            logger.info(s"device already exist: ${dev.deviceName}")
          case None =>
            val dev = Device(
              deviceId = UUIDUtil.uuidStr,
              owners = Set.empty,
              groups = Set(defaultGroup),
              deviceTypeKey = dtype,
              hwDeviceId = di.hwDeviceId,
              hashedHwDeviceId = HashUtil.sha512Base64(di.hwDeviceId.toLowerCase()),
              deviceName = s"$dtype ${di.hwDeviceId}",
              pubRawQueues = Some(Set(
                Config.awsSqsQueueTransformer,
                rawQueue
              )),
              deviceProperties = Some(
                DeviceTypeUtil.defaultProps(dtype)
              ),
              deviceConfig = Some(
                DeviceTypeUtil.defaultConf(dtype)
              ),
              tags = DeviceTypeUtil.defaultTags(dtype)
            )
            DeviceManager.create(device = dev)
            logger.info(s"device: $dev")
        }
      }
    }
  }

  private def getDeviceTypeKey(deviceType: String): Future[String] = {
    DeviceTypeManager.getByKey(deviceType).map {
      case Some(dtype) =>
        dtype.key
      case None =>
        Const.TRACKLESENSOR
    }
  }

}
