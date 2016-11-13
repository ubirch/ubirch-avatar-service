package com.ubirch.avatar.cmd

import java.io.File
import java.net.URL

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.avatar.util.model.StorageCleanup
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.services.util.DeviceUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.{Config, HttpClient}

import scala.collection._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.{Codec, Source}
import scala.language.postfixOps

/**
  * Created by derMicha on 13/11/16.
  */
object ImportTrackle extends App with LazyLogging with StorageCleanup {

  /**
    * may be you have to fix that, usually Google Drive is a root folder inside your home folder
    */
  val googleDriveBasePath = s"${System.getProperty("user.home")}/"

  val httpClient = new HttpClient(commonConfig = Config(
    connectTimeout = 15000,
    readTimeout = 15000
  ))

  val avatarServiceUrl = "http://localhost:8080/api/avatarService/v1/device/bulk"

  val hwDeviceId = UUIDUtil.uuidStr
  val hashedHwDeviceId = HashUtil.sha512Base64(hwDeviceId)

  cleanElasticsearch

  val device = Device(
    deviceId = UUIDUtil.uuidStr,
    deviceName = "trackle Sensor 001",
    hwDeviceId = hwDeviceId,
    deviceTypeKey = Const.TRACLESENSOR
  )

  //    Await.result(DeviceManager.createWithShadow(device), 5 seconds)
  Await.result(DeviceManager.create(device), 5 seconds)

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

    def dateTime = DateTime.parse(s"${day}T${time}")
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

  val allDataFilenames = s"${googleDriveBasePath}Google Drive/trackle/Tests Sophie/rawdataFiles/testLogData/allDatafiles.txt"

  val adf = Source.fromFile(allDataFilenames)(Codec.UTF8)
  adf.getLines().foreach { dfnLine =>
    val dfnLineSplitted = dfnLine.split(";")
    val basePath = dfnLineSplitted(0)
    val logFilename = dfnLineSplitted(1)
    val csvFilename = dfnLineSplitted(2)

    val csvFile = new File(s"$googleDriveBasePath$basePath/$csvFilename")
    val logFile = new File(s"$googleDriveBasePath$basePath/$logFilename")

    if (csvFile.exists() && logFile.exists()) {

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
          Json4sUtil.any2jvalue(ldp) match {
            case Some(payload) =>
              val ddr = DeviceDataRaw(
                id = UUIDUtil.uuid,
                v = "0.0.1",
                a = hashedHwDeviceId,
                ts = cdp.dateTime,
                s = DeviceUtil.createSimpleSignature(payload, hwDeviceId),
                p = payload
              )

              //TODO use throtteling, akka offers this for free
              val ddrString = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(ddr).get)
              val body = RequestBody(ddrString, APPLICATION_JSON)
              val resp = httpClient.post(new URL(avatarServiceUrl), Some(body))
            //              DeviceDataRawManager.store(ddr)
            //                  logger.debug(s"$ddr")
            case None =>
              logger.error(s"could not parse payload: $ldp")
          }
        }
      }
    }
    else {
      logger.error("one file is missing")
      if (!csvFile.exists())
        logger.error(s"csvFile missing: ${csvFile.getAbsolutePath}")
      if (!logFile.exists())
        logger.error(s"logFile missing: ${logFile.getAbsolutePath}")
    }
  }
}
