package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.{DeviceManager, DeviceTypeManager}
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.io.Source

object ImportProdLogs
  extends App
    with StrictLogging {

  val conf = ConfigFactory.load()

  implicit val system = ActorSystem("AvatarService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  case class DeviceInfo(
                         deviceType: String,
                         testTimestamp: String,
                         testResult: Boolean,
                         hwDeviceId: String,
                         firmwareVersion: String,
                         orderNr: String,
                         tester: String
                       )

  val basePath = conf.getString("importProdLogs.basePath")

  val prodLogs = conf.getStringList("importProdLogs.prodLogs").asScala.toSet

  val defaultGroup = UUIDUtil.uuid

  val deleteExistingDevices = conf.getBoolean("importProdLogs.deleteExistingDevices")

  val sep = "\t"

  val envId = conf.getString("importProdLogs.envId")
  val rawQueue1 = conf.getString("importProdLogs.rawQueue1")
  val queue1 = conf.getString("importProdLogs.queue1")
  val rawQueue2 = conf.getString("importProdLogs.rawQueue2")

  val deviceTypeOffset = 0
  val deviceTestDatetimeOffset = 1
  val testResultOffset = 5
  val hwDeviceIdOffset = 7
  val firmwareVersionOffset = 9
  val orderNrOffset = 11
  val testerOffset = 12

  try {

    prodLogs.foreach { fn =>
      val deviceRows = Source.fromFile(s"$basePath/$fn").getLines().toList.tail
      deviceRows.foreach { row =>
        val rowData = row.split(sep)

        val di = DeviceInfo(
          deviceType = rowData(deviceTypeOffset).toString,
          testTimestamp = rowData(deviceTestDatetimeOffset).toString,
          testResult = !rowData(testResultOffset).toString.toLowerCase.contains("failed"),
          hwDeviceId = rowData(hwDeviceIdOffset).toString,
          firmwareVersion = rowData(firmwareVersionOffset).toString,
          orderNr = rowData(orderNrOffset).toString,
          tester = rowData(testerOffset).toString
        )
        logger.info(s"deviceInfo: ${di.deviceType} / ${di.testResult} / ${di.hwDeviceId}")
        getDeviceTypeKey(di.deviceType).map { dtype =>
          DeviceManager.infoByHwId(di.hwDeviceId).map {
            case Some(dev) =>
              logger.info(s"device already exist: ${dev.deviceName}")
              if (deleteExistingDevices) {
                DeviceManager.delete(dev)
                logger.info(s"device deleted: ${dev.deviceName}")
              }
            case None =>
              val dev = Device(
                deviceId = UUIDUtil.uuidStr,
                owners = Set.empty,
                groups = Set(defaultGroup),
                deviceTypeKey = dtype,
                hwDeviceId = di.hwDeviceId,
                hashedHwDeviceId = HashUtil.sha512Base64(di.hwDeviceId.toLowerCase()),
                deviceName = s"$dtype ${di.hwDeviceId}",
                pubQueues = Some(Set(queue1)),
                pubRawQueues = Some(Set(
                  rawQueue1,
                  rawQueue2
                )),
                deviceProperties = Some(
                  DeviceTypeUtil.defaultProps(dtype)
                    merge
                    Json4sUtil.any2jvalue(Map[String, Any](
                      "testedFirmwareVersion" -> s"${di.firmwareVersion}",
                      "tester" -> s"${di.tester}",
                      "testerResult" -> di.testResult,
                      "orderNr" -> s"${di.orderNr}"
                    )).get
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

  } finally {
    shutdown(3000 + prodLogs.size * 100)
  }

  private def getDeviceTypeKey(deviceType: String): Future[String] = {
    DeviceTypeManager.getByKey(deviceType).map {
      case Some(dtype) =>
        dtype.key
      case _ =>
        Const.TRACKLESENSOR
    }
  }

  private def shutdown(sleep: Int): Unit = {
    Thread.sleep(sleep)
    system.terminate()
    Thread.sleep(500)
  }

}
