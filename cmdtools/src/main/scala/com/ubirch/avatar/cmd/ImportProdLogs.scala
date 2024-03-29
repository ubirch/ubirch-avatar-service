package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.{ ConfigKeys, Const }
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{ DateTime, DateTimeZone }

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutor
import scala.io.Source
import scala.util.{ Failure, Success }

object ImportProdLogs extends App with StrictLogging {

  val conf = ConfigFactory.load()

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  implicit val system: ActorSystem = ActorSystem("AvatarService")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  case class DeviceInfo(
    deviceType: String,
    testTimestamp: DateTime,
    testResult: Boolean,
    hwDeviceId: String,
    tempSensorId: String,
    firmwareVersion: String,
    orderNr: String,
    tester: String
  )

  val basePath = conf.getString("importProdLogs.basePath")

  val prodLogs = conf.getStringList("importProdLogs.prodLogs").asScala.toSet[String]

  val defaultGroups =
    conf.getStringList("importProdLogs.devcieAdminGroup").asScala.toSet[String].map(us => UUIDUtil.fromString(us))

  val deleteExistingDevices = conf.getBoolean("importProdLogs.deleteExistingDevices")
  val createMissingDevices = conf.getBoolean("importProdLogs.createMissingDevices")

  val envId = conf.getString("importProdLogs.envId")
  val rawQueue1 = conf.getString("importProdLogs.rawQueue1")
  val queue1 = conf.getString("importProdLogs.queue1")
  val rawQueue2 = conf.getString("importProdLogs.rawQueue2")

  val deviceTypeOffset = 0
  val deviceTestDatetimeOffset = 1
  val testResultOffset = 5
  val hwDeviceIdOffset = 7
  val hwDeviceIdUUIDOffset = 13
  val tempSensorIdOffset = 8
  val firmwareVersionOffset = 9
  val orderNrOffset = 11
  val testerOffset = 12

  def isValidHex(value: String): Boolean = {
    val hexChars = "1234567890abcdef"
    value.toLowerCase.forall(c => hexChars.contains(c))
  }

  try {

    prodLogs.foreach { fn =>
      val deviceRows = Source.fromFile(s"$basePath/$fn").getLines().toList.tail
      deviceRows.foreach { row =>
        val sep = if (fn.endsWith(".tsv")) "\t" else ","
        val rowData = row.split(sep)

        val hwDeviceId =
          if (rowData.size >= hwDeviceIdUUIDOffset + 1)
            rowData(hwDeviceIdUUIDOffset).toLowerCase
          else {
            val rawHwDid = rowData(hwDeviceIdOffset).toLowerCase
            if (rawHwDid.length == 32 && isValidHex(rawHwDid))
              s"${rawHwDid.take(16)}-${rawHwDid.takeRight(16)}"
            else
              rawHwDid
          }

        val di = DeviceInfo(
          deviceType = rowData(deviceTypeOffset),
          testTimestamp = DateTime.parse(rowData(deviceTestDatetimeOffset)).withZone(DateTimeZone.UTC),
          testResult = !rowData(testResultOffset).toLowerCase.contains("failed"),
          hwDeviceId = hwDeviceId,
          tempSensorId = rowData(tempSensorIdOffset),
          firmwareVersion = rowData(firmwareVersionOffset),
          orderNr = rowData(orderNrOffset),
          tester = rowData(testerOffset)
        )
        logger.info(
          s"deviceInfo: ${di.hwDeviceId} / type: ${di.deviceType} / exists: ${di.testResult} / testResult: ${rowData(testResultOffset)}")
        val dtype = Const.TRACKLESENSOR
        DeviceManager.infoByHwId(di.hwDeviceId).onComplete {
          case Success(devOpt) =>
            devOpt match {
              case Some(dev) =>
                logger.info(s"device already exist: ${dev.hwDeviceId}")
                if (deleteExistingDevices) {
                  DeviceManager.delete(dev)
                  logger.info(s"device deleted: ${dev.hwDeviceId}")
                } else {
                  if (dev.deviceProperties.isDefined) {
                    val devUpdated = dev.copy(
                      deviceProperties = Some(
                        dev.deviceProperties.get
                          merge
                            Json4sUtil.any2jvalue(Map[String, Any](
                              "testTimestamp" -> s"${di.testTimestamp}"
                            )).get)
                    )
                    DeviceManager.update(devUpdated)
                  }
                }

              case None =>
                if (createMissingDevices && di.testResult) {
                  val dev = Device(
                    deviceId = UUIDUtil.uuidStr,
                    owners = Set.empty,
                    groups = defaultGroups,
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
                      DeviceTypeUtil.defaultProps
                        merge
                          Json4sUtil.any2jvalue(Map[String, Any](
                            "testedFirmwareVersion" -> s"${di.firmwareVersion}",
                            "tester" -> s"${di.tester}",
                            "testerResult" -> di.testResult,
                            "orderNr" -> s"${di.orderNr}",
                            "tempSensorId" -> s"${di.tempSensorId}",
                            "testTimestamp" -> s"${di.testTimestamp}"
                          )).get
                    ),
                    deviceConfig = Some(
                      DeviceTypeUtil.defaultConf
                    ),
                    tags = DeviceTypeUtil.defaultTags
                  )
                  DeviceManager.create(device = dev).onComplete {
                    case Success(newDev) =>
                      newDev match {
                        case None =>
                          logger.error("could not create device")
                        case Some(d) =>
                          logger.info(s"device: $d")
                      }
                    case Failure(t) =>
                      logger.error(s"failed: ${t.getMessage}")

                  }
                } else
                  logger.debug(s"device not created: ${di.hwDeviceId}")
            }
          case Failure(t) =>
            logger.error(s"fetch device failed: ${t.getMessage}")
        }
      }
    }
  } catch {
    case e: Exception =>
      logger.error("something kapuuuth:", e)
  }

  private def shutdown(sleep: Int): Unit = {
    Thread.sleep(sleep)
    system.terminate()
    Thread.sleep(500)
  }

}
