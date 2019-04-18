package com.ubirch.avatar.cmd

import java.io.File
import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.client.rest.AvatarRestClient
import com.ubirch.avatar.client.rest.config.AvatarClientConfig
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.model.rest.payload.TrackleSensorPayload
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.transformer.services.PtxTransformerService
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import uk.co.bigbeeconsultants.http.response.Status

import scala.collection._
import scala.io.{Codec, Source}
import scala.language.postfixOps

/**
  * author: derMicha
  */
object ImportTrackle extends App
  with StrictLogging {

  val userId = UUID.fromString("884e57da-e02f-415a-bfab-7d6bb0b7ed44")

  /**
    * may be you have to fix that, usually Google Drive is a root folder inside your home folder
    */
  private val googleDriveBasePath = s"${System.getProperty("user.home")}/"

  //  private val hwDeviceId = UUIDUtil.uuidStr

  //private val hwDeviceId = "123123"
  //private val hashedHwDeviceId = HashUtil.sha512Base64(hwDeviceId)
  private val ts = new DateTime().toLocalDateTime.toString

  //private val deviceProps: JValue = parse(
  //    s"""{
  //       |  "${Const.STOREDATA}": true,
  //       |  "${Const.BLOCKC}": false
  //       |}""".stripMargin
  //  )
  //
  //  private val pubQ = Some(immutable.Set(
  //    "local_dev_trackle_avatar_service_transformer_outbox"
  //  ))

  //  private val device = Device(
  //    deviceId = UUIDUtil.uuidStr,
  //    owners = Set(userId),
  //    deviceName = s"trackle Sensor $ts",
  //    hwDeviceId = hwDeviceId,
  //    deviceTypeKey = Const.TRACKLESENSOR,
  //    deviceProperties = Some(deviceProps),
  //    pubQueues = pubQ
  //  )

  AvatarClientConfig.userToken match {

    case None =>
      logger.error("unable to import trackle data if auth token is not configured (see config key 'ubirchAvatarService.cmdTools.userToken'")

    case Some(oidcToken) =>
      logger.info(s"start import for $hwDeviceId / $hashedHwDeviceId")
      importData(oidcToken)
    //      if (createDevice(device, oidcToken)) {
    //        importData(oidcToken)
    //      }

  }

  private def createDevice(device: Device, oidcToken: String): Boolean = {

    val deviceCreationResponse = AvatarRestClient.devicePOST(device, oidcToken = Some(oidcToken))
    // TODO migrate to AvatarSvcClientRest
    // see `AvatarSvcClientRestSpec` for example instantiating http client and materializer
    //val deviceCreationResponse = AvatarSvcClientRest.devicePOST(device, oidcToken = Some(oidcToken))

    if (deviceCreationResponse.status != Status.S200_OK) {
      logger.error(s"failed to create device: response=$deviceCreationResponse")
    }

    deviceCreationResponse.status == Status.S200_OK

  }

  private def importData(oidcToken: String): Unit = {

    //  private val allDataFiles = s"${googleDriveBasePath}Google Drive/trackle/Tests Sophie/rawdataFiles/testLogData/allDatafiles.txt"
    val allDataFiles = "./data/datafiles.txt"

    val adf = Source.fromFile(allDataFiles)(Codec.UTF8)
    adf.getLines().foreach { dfnLine =>

      val dfnLineSplit = dfnLine.split(";")

      val rootPath = dfnLineSplit(0)
      val dayString = dfnLineSplit(1)

      val basePath = s"$rootPath/$dayString"
      val csvFilename = s"${dayString}_trackle_log.csv"
      val logFilename = s"${dayString}_terminal.txt"

      //    val csvFile = new File(s"$googleDriveBasePath$basePath/$csvFilename")
      val csvFile = new File(s"$basePath/$csvFilename")
      //    val logFile = new File(s"$googleDriveBasePath$basePath/$logFilename")
      val logFile = new File(s"$basePath/$logFilename")

      if (csvFile.exists() && logFile.exists()) {

        importCsvFile(oidcToken, csvFile = csvFile, logFile = logFile)

      } else {

        logger.error("one file is missing")
        if (!csvFile.exists()) {
          logger.error(s"csvFile missing: ${csvFile.getAbsolutePath}")
        }
        if (!logFile.exists()) {
          logger.error(s"logFile missing: ${logFile.getAbsolutePath}")
        }

      }

    }

  }

  private def importCsvFile(oidcToken: String,
                            csvFile: File,
                            logFile: File
                           ): Unit = {

    val csvData: mutable.HashMap[String, CsvData] = new mutable.HashMap()
    val logData: mutable.HashMap[String, LogData] = new mutable.HashMap()

    val csvSource = Source.fromFile(csvFile)(Codec.UTF8)
    csvSource.getLines().foreach { line =>

      val splitted = line.split(";")
      val csvDatapoint = CsvData.parseData(splitted)
      csvData += (csvDatapoint.id -> csvDatapoint)
    }

    val logSource = Source.fromFile(logFile)(Codec.UTF8)
    logSource.getLines().filter(l => l.contains(" TR") && l.contains("mV") && l.contains("T1adc")).foreach { line =>
      val logDatapoint = LogData.parseData(line)
      logData += (logDatapoint.id -> logDatapoint)
    }

    csvData.keySet.toList.sorted.foreach { key =>
      val csvDataPoint = csvData.get(key)
      val logDataPoint = logData.get(key)
      if (logDataPoint.isDefined && csvDataPoint.isDefined) {
        val cdp = csvDataPoint.get
        val ldp = logDataPoint.get

        val t1 = PtxTransformerService.pt100_temperature(ldp.t1Adc)
        val t2 = PtxTransformerService.pt100_temperature(ldp.t2Adc)

        val tracklePayload = TrackleSensorPayload(
          ts = cdp.dateTime,
          t = (((t1 + t2) / 2)*100).toInt,
          cy = cdp.paketCounter,
          er = 0
        )

        Json4sUtil.any2jvalue(tracklePayload) match {

          case Some(payload) =>
            val ddr = DeviceDataRaw(
              id = UUIDUtil.uuid,
              v = MessageVersion.v001,
              a = hashedHwDeviceId,
              ts = cdp.dateTime,
              s = Some(DeviceCoreUtil.createSimpleSignature(payload, hwDeviceId)),
              p = payload
            )

            val ddrBulkResponse = AvatarRestClient.deviceUpdateBulkPOST(ddr, oidcToken = Some(oidcToken))
            // TODO migrate to AvatarSvcClientRest
            // see `AvatarSvcClientRestSpec` for example instantiating http client and materializer
            //val ddrBulkResponse = AvatarSvcClientRest.deviceUpdateBulkPOST(ddr)
            if (ddrBulkResponse.status == Status.S200_OK) {
              logger.info(s"data created")
            } else {
              logger.error(s"failed to create data: $ddrBulkResponse")
            }
            Thread.sleep(100)

          case None => logger.error(s"could not parse payload: $ldp")

        }
      }
    }

  }

}

/**
  * 1. TrackleId,: 1 (0 or 1)
  *2. Paket Count: 179 (1 Byte, max 255)
  *3. Timestamp: 2271 (0-65535)
  *4. T1  (Temp in °C, formula by Delta, see below)
  *5. T2  (Temp in °C, formula by Delta, see below)
  *6. T3 (Temp in °C, formula by Delta, see below)
  *7. Day
  *8. Time
  */
case class CsvData(
                    trackleId: String,
                    paketCounter: Int,
                    timestamp: Int,
                    t1: Double,
                    t2: Double,
                    t3: Double,
                    day: String,
                    time: String
                  ) {
  override def toString: String = s"$trackleId / $timestamp / $paketCounter"

  def id: String = s"$trackleId-$timestamp"

  def dateTime: DateTime = DateTime.parse(s"${day}T$time")
}

object CsvData {
  def parseData(data: Array[String]): CsvData = {
    CsvData(
      trackleId = data(0),
      paketCounter = data(1).toInt,
      timestamp = data(2).toInt,
      t1 = data(3).toDouble,
      t2 = data(4).toDouble,
      t3 = data(5).toDouble,
      day = data(6),
      time = data(7)
    )
  }
}

/**
  *
  *1. Timestamp: 2271 (0-65535)
  *2. TrackleId: TR1 (01 oder 1)
  *3. Akkuladung: 1308mV
  *4. Paket Count: 117 (1 Byte, max 255)
  *5. T1adc: 22192 (Widerstand in Ohm)
  *6. T2adc: 22103 (Widerstand in Ohm)
  *7. T1r: 114.285 (resistance, formula: ((((adc * 1.35) / 65536) / 0.0005) / 8.0)   )
  *8. T2r: 113.827 (resistance, formula: ((((adc * 1.35) / 65536) / 0.0005) / 8.0)
  *9. T1: 36.75 (Temp in °C, formula by Delta, see below)
  *10. T2: 35.57 (Temp in °C, formula by Delta, see below)
  *11. T3: 36.22C (currently not relevant)
  *12. T = avg
  */
case class LogData(
                    timestamp: Int,
                    trackleId: String,
                    batteryPower: Int,
                    paketCounter: Int,
                    t1Adc: Int,
                    t2Adc: Int,
                    t1r: Double,
                    t2r: Double,
                    t1: Double,
                    t2: Double,
                    t3: Double,
                    tAvg: Double
                  ) {
  override def toString: String = s"$trackleId / $timestamp / $paketCounter"

  def id: String = s"$trackleId-$timestamp"
}

object LogData {
  def parseData(data: String): LogData = {

    val t1 = data.substring(101, 106).trim.toDouble
    val t2 = data.substring(116, 121).trim.toDouble
    val t3 = data.substring(131, 136).trim.toDouble

    LogData(
      timestamp = data.substring(3, 7).trim.toInt,
      trackleId = data.substring(10, 11).trim,
      batteryPower = data.substring(13, 17).trim.toInt,
      paketCounter = data.substring(23, 26).trim.toInt,
      t1Adc = data.substring(37, 42).trim.toInt,
      t2Adc = data.substring(54, 59).trim.toInt,
      t1r = data.substring(69, 76).trim.toDouble,
      t2r = data.substring(85, 92).trim.toDouble,
      t1 = t1,
      t2 = t2,
      t3 = t3,
      tAvg = (t1 + t2) / 2

    )
  }
}
