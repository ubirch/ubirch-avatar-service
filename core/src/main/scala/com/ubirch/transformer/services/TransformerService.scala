package com.ubirch.transformer.services

import java.text.NumberFormat
import java.util.Locale

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceHistory, DeviceType}
import com.ubirch.avatar.model.rest.payload._
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.JValue

import scala.concurrent.ExecutionContext

/**
  * Created by derMicha on 29/11/16.
  */

case class LocationSnippet(location: GeoLocation)

object TransformerService
  extends StrictLogging
    with MyJsonProtocol {

  /**
    *
    * @param deviceType deviceType object for current device
    * @param device     ubirch device
    * @param drd        deviceDataRaw
    * @param sdrd       deviceDataRaw source
    * @return DeviceHistory
    */
  def transform(deviceType: DeviceType, device: Device, drd: DeviceDataRaw, sdrd: DeviceDataRaw)(implicit ec: ExecutionContext): Option[DeviceHistory] = {

    logger.debug(s"transform data from $deviceType / $device")

    val nf = NumberFormat.getInstance(Locale.US)

    //@TODO this is ugly !!!
    val (transformedPayload: Option[JValue], timestamp: Option[DateTime]) =
      if (device.deviceTypeKey == Const.ENVIRONMENTSENSOR)
        drd.p.extractOpt[EnvSensorRawPayload] match {
          case Some(envRawP) =>
            val envP = EnvSensorPayload(
              temperature = envRawP.t.toDouble / 100.0,
              presure = envRawP.p.toDouble / 100.0,
              humidity = envRawP.h.toDouble / 100.0,
              batteryLevel = envRawP.ba,
              location = if (envRawP.la.isDefined && envRawP.lo.isDefined) Some(GeoLocation(envRawP.la.get.toDouble, envRawP.la.get.toDouble)) else None,
              altitude = if (envRawP.a.isDefined) Some(envRawP.a.get / 100.0) else None,
              loops = if (envRawP.lp.isDefined) Some(envRawP.lp.get) else None,
              errorCode = if (envRawP.e.isDefined) Some(envRawP.e.getOrElse(0)) else None,
              timestamp = envRawP.ts
            )

            Json4sUtil.any2jvalue(envP) match {
              case Some(jval) =>
                (Some(jval), envP.timestamp)
              case _ =>
                (Some(drd.p), None)
            }
          case _ =>
            logger.error("invalid envSensore payload")
            (Some(drd.p), None)
        }
      else if (device.deviceTypeKey == Const.AQSENSOR) {
        drd.p.extractOpt[AqSensorRawPayload] match {
          case Some(aqRawP) =>

            val aqP = AqSensorPayload(
              airquality = aqRawP.aq,
              airqualityRef = aqRawP.aqr,
              temperature = aqRawP.t.toDouble / 100.0,
              presure = aqRawP.p.toDouble / 100.0,
              humidity = aqRawP.h.toDouble / 100.0,
              batteryLevel = aqRawP.ba,
              location = if (aqRawP.la.isDefined && aqRawP.lo.isDefined) Some(GeoLocation(aqRawP.la.get.toDouble, aqRawP.la.get.toDouble)) else None,
              altitude = if (aqRawP.a.isDefined) Some(aqRawP.a.get / 100.0) else None,
              loops = if (aqRawP.lp.isDefined) Some(aqRawP.lp.get) else None,
              errorCode = if (aqRawP.e.isDefined) Some(aqRawP.e.getOrElse(0)) else None,
              timestamp = aqRawP.ts
            )

            Json4sUtil.any2jvalue(aqP) match {
              case Some(jval) =>
                (Some(jval), aqP.timestamp)
              case _ =>
                (Some(drd.p), None)
            }

          case _ =>
            logger.error("invalid aqSensore payload")
            (Some(drd.p), None)
        }
      }
      else if (device.deviceTypeKey == Const.EMOSENSOR) {
        drd.p.extractOpt[EmoSensorRawPayload] match {
          case Some(emoRawP) =>

            val emoP = EmoSensorPayload(
              temperature = emoRawP.tmp.toDouble / 100.00,
              emg = emoRawP.emg,
              gsr = emoRawP.gsr,
              pulse = emoRawP.pls,
              activity = emoRawP.act,
              emoDeviceId = emoRawP.did,
              messageId = emoRawP.mid,
              batteryLevel = emoRawP.bat,
              errorCode = if (emoRawP.e.isDefined) Some(emoRawP.e.getOrElse(0)) else None,
              timestamp = emoRawP.ts
            )

            Json4sUtil.any2jvalue(emoP) match {
              case Some(jval) =>
                (Some(jval), emoP.timestamp)
              case _ =>
                (Some(drd.p), None)
            }

          case _ =>
            logger.error("invalid aqSensore payload")
            (Some(drd.p), None)
        }
      }
      else if (device.deviceTypeKey == Const.TRACKLESENSOR)
        drd.p.extractOpt[TrackleSensorPayload] match {
          case Some(tracklePayload) =>
            val trackleP = TrackleSensorMeasurement(
              mid = drd.id,
              did = device.deviceId,
              ts = tracklePayload.ts,
              te = tracklePayload.t.toDouble / 100,
              er = tracklePayload.er
            )
            Json4sUtil.any2jvalue(trackleP) match {
              case Some(jval) =>
                (Some(jval), Some(trackleP.ts))
              case _ =>
                (Some(drd.p), None)
            }
          case _ =>
            logger.error("invalid trackle payload")

            (Some(drd.p), None)
        }
      else {
        logger.debug(s"start parsing data for device: ${drd.deviceId}")
        val tsDT = (drd.p \ "ts").extractOpt[DateTime]
        val tsLong = (drd.p \ "ts").extractOpt[Long]
        val ts = if (tsDT.isDefined)
          tsDT
        else if (tsLong.isDefined)
          Some(new DateTime(tsLong.get, DateTimeZone.UTC))
        else
          None

        val la = (drd.p \ "la").extractOpt[String]
        val lo = (drd.p \ "lo").extractOpt[String]

        logger.debug(s"found ts $ts for device: ${drd.deviceId}")

        try {
          val patchedPay = if (la.isDefined && lo.isDefined) {
            logger.debug("found lo/la")
            val geo = LocationSnippet(location = GeoLocation(
              lat = nf.parse(la.get).doubleValue(),
              lon = nf.parse(lo.get).doubleValue()
            ))
            drd.p merge Json4sUtil.any2jvalue(geo).get
          }
          else
            drd.p

          (Some(patchedPay), ts)
        }
        catch {
          case e: Exception =>
            logger.error(s"error parsing lo/la for: ${drd.did}", e)
            (Some(drd.p), ts)
        }
      }

    if (transformedPayload.isDefined)
      Some(DeviceHistory(
        messageId = UUIDUtil.uuid,
        deviceDataRawId = sdrd.id,
        deviceId = device.deviceId,
        deviceName = device.deviceName,
        deviceType = deviceType.key,
        deviceTags = device.tags,
        deviceMessage = transformedPayload.get,
        deviceDataRaw = Some(sdrd),
        timestamp = if (timestamp.isDefined) timestamp.get else drd.ts
      ))
    else {
      logger.error(s"error parsing data for device: ${drd.deviceId} / ${drd.p}")
      None
    }
  }
}