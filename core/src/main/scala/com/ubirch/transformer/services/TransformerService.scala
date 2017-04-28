package com.ubirch.transformer.services

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.device._
import com.ubirch.avatar.model.payload._
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

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
  def transform(deviceType: DeviceType, device: Device, drd: DeviceDataRaw, sdrd: DeviceDataRaw)(implicit ec: ExecutionContext): Option[DeviceDataProcessed] = {

    logger.debug(s"$deviceType / $device")

    val transformedPayload = if (device.deviceTypeKey == Const.ENVIRONMENTSENSOR)
      drd.p.extractOpt[EnvSensorRawPayload] match {
        case Some(envRawP) =>
          val envP = EnvSensorPayload(
            temperature = envRawP.t.toDouble / 100.0,
            presure = envRawP.p.toDouble / 100.0,
            humidity = envRawP.h.toDouble / 100.0,
            batteryLevel = envRawP.ba,
            latitude = if (envRawP.la.isDefined) Some(envRawP.la.get.toDouble) else None,
            longitude = if (envRawP.lo.isDefined) Some(envRawP.lo.get.toDouble) else None,
            altitude = if (envRawP.a.isDefined) Some(envRawP.a.get.toDouble / 100.0) else None,
            loops = if (envRawP.lp.isDefined) Some(envRawP.lp.get) else None,
            errorCode = if (envRawP.e.isDefined) Some(envRawP.e.getOrElse(0)) else None
          )

          Json4sUtil.any2jvalue(envP) match {
            case Some(jval) =>
              Some(jval)
            case _ =>
              Some(drd.p)
          }
        case _ =>
          logger.error("invalid envSensore payload")
          Some(drd.p)
      }
    else if (device.deviceTypeKey == Const.AQSENSOR) {
      drd.p.extractOpt[AqSensorRawPayload] match {
        case Some(aqRawP) =>
          if (aqRawP.aqr > 0) {

            val envP = AqSensorPayload(
              airquality = aqRawP.aq,
              airqualityRef = aqRawP.aqr,
              temperature = aqRawP.t.toDouble / 100.0,
              presure = aqRawP.p.toDouble / 100.0,
              humidity = aqRawP.h.toDouble / 100.0,
              batteryLevel = aqRawP.ba,
              latitude = if (aqRawP.la.isDefined) Some(aqRawP.la.get.toDouble) else None,
              longitude = if (aqRawP.lo.isDefined) Some(aqRawP.lo.get.toDouble) else None,
              altitude = if (aqRawP.a.isDefined) Some(aqRawP.a.get.toDouble / 100.0) else None,
              loops = if (aqRawP.lp.isDefined) Some(aqRawP.lp.get) else None,
              errorCode = if (aqRawP.e.isDefined) Some(aqRawP.e.getOrElse(0)) else None
            )

            Json4sUtil.any2jvalue(envP) match {
              case Some(jval) =>
                Some(jval)
              case _ =>
                Some(drd.p)
            }
          }
          else {
            logger.error("aqSensore not initialized")
            None
          }

        case _ =>
          logger.error("invalid aqSensore payload")
          Some(drd.p)
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
            e = tracklePayload.e
          )
          Json4sUtil.any2jvalue(trackleP) match {
            case Some(jval) =>
              Some(jval)
            case _ =>
              Some(drd.p)
          }
        case _ =>
          logger.error("invalid trackle payload")
          Some(drd.p)
      }
    else
      Some(drd.p)

    if (transformedPayload.isDefined)
      Some(DeviceDataProcessed(
        messageId = drd.id,
        deviceDataRawId = sdrd.id,
        deviceId = device.deviceId,
        deviceName = device.deviceName,
        deviceType = deviceType.key,
        deviceTags = device.tags,
        deviceMessage = transformedPayload.get,
        deviceDataRaw = Some(sdrd),
        timestamp = drd.ts
      ))
    else
      None
  }
}
