package com.ubirch.transformer.services

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceHistory, DeviceType}
import com.ubirch.avatar.model.rest.payload.{AqSensorPayload, AqSensorRawPayload, EmoSensorPayload, EmoSensorRawPayload, EnvSensorPayload, EnvSensorRawPayload, TrackleSensorPayload, TrackleSensorPayloadOut}
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import org.json4s.JsonAST.JValue

import scala.concurrent.ExecutionContext

/**
  * Created by derMicha on 29/11/16.
  */
object TransformerService
  extends StrictLogging
    with MyJsonProtocol {

  /**
    *
    * @param deviceType deviceType object for current device
    * @param device
    * @param drd
    * @param sdrd
    * @return
    */
  def transform(deviceType: DeviceType, device: Device, drd: DeviceDataRaw, sdrd: DeviceDataRaw)(implicit ec: ExecutionContext): Option[DeviceHistory] = {

    logger.debug(s"transform data from $deviceType / $device")
    //@TODO this is ugly !!!
    val (transformedPayload: Option[JValue], timestamp: Option[DateTime]) = if (device.deviceTypeKey == Const.ENVIRONMENTSENSOR)
      drd.p.extractOpt[EnvSensorRawPayload] match {
        case Some(envRawP) =>
          val envP = EnvSensorPayload(
            temperature = envRawP.t.toDouble / 100.0,
            presure = envRawP.p.toDouble / 100.0,
            humidity = envRawP.h.toDouble / 100.0,
            batteryLevel = envRawP.ba,
            latitude = if (envRawP.la.isDefined) Some(envRawP.la.get.toDouble) else None,
            longitude = if (envRawP.lo.isDefined) Some(envRawP.lo.get.toDouble) else None,
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
            latitude = if (aqRawP.la.isDefined) Some(aqRawP.la.get.toDouble) else None,
            longitude = if (aqRawP.lo.isDefined) Some(aqRawP.lo.get.toDouble) else None,
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
          val trackleP = TrackleSensorPayloadOut(
            ts = tracklePayload.ts,
            ba = tracklePayload.ba,
            pc = tracklePayload.pc,
            t1Adc = tracklePayload.t1,
            t2Adc = tracklePayload.t2,
            t3Adc = tracklePayload.t3,
            t1 = PtxTransformerService.pt100_temperature(tracklePayload.t1).toDouble,
            t2 = PtxTransformerService.pt100_temperature(tracklePayload.t2).toDouble,
            t3 = PtxTransformerService.pt100_temperature(tracklePayload.t3).toDouble,
            la = tracklePayload.la,
            lo = tracklePayload.lo,
            e = tracklePayload.e,
            dt = tracklePayload.dt
          )
          Json4sUtil.any2jvalue(trackleP) match {
            case Some(jval) =>
              (Some(jval), trackleP.dt)
            case _ =>
              (Some(drd.p), None)
          }
        case _ =>
          logger.error("invalid trackle payload")
          (Some(drd.p), None)
      }
    else
      (Some(drd.p), None)

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
    else
      None
  }
}
