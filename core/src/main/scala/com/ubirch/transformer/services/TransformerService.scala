package com.ubirch.transformer.services

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.device._
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

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
  def transform(deviceType: DeviceType, device: Device, drd: DeviceDataRaw, sdrd: DeviceDataRaw): DeviceDataProcessed = {

    logger.debug(s"$deviceType / $device")

    val tp = if (deviceType.key == Const.ENVIRONMENTSENSOR)
      drd.p.extractOpt[EnvSensorRawPayload] match {
        case Some(envRawP) =>
          val envP = EnvSensorPayload(
            temperature = envRawP.t / 100,
            presure = envRawP.p / 100,
            humidity = envRawP.h / 100,
            batteryLevel = envRawP.ba,
            latitude = envRawP.la.toDouble,
            longitude = envRawP.lo.toDouble,
            altitude = envRawP.a / 100,
            loops = envRawP.lp,
            errorCode = envRawP.e
          )

          Json4sUtil.any2jvalue(envP) match {
            case Some(jval) =>
              jval
            case _ =>
              drd.p
          }
        case _ =>
          drd.p
      }
    else
      drd.p

    DeviceDataProcessed(
      deviceId = device.deviceId,
      messageId = drd.id,
      deviceDataRawId = sdrd.id,
      deviceType = deviceType.key,
      timestamp = drd.ts,
      deviceTags = device.tags,
      deviceMessage = tp,
      deviceDataRaw = Some(sdrd)
    )
  }
}
