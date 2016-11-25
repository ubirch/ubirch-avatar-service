package com.ubirch.avatar.core.device

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.awsiot.services.AwsShadowService
import com.ubirch.avatar.model.device.{Device, DeviceStateUpdate}
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.uuid.UUIDUtil

import org.json4s._
import org.json4s.native.Serialization._

/**
  * Created by derMicha on 09/11/16.
  */
object DeviceStateManager extends MyJsonProtocol with StrictLogging {

  def currentDeviceState(device: Device): DeviceStateUpdate = {

    val payload = AwsShadowService.getDelta(device.awsDeviceThingId) match {
      case Some(pl) =>
        pl
      case None =>
        read[JValue]("")
    }

    val (k, s) = DeviceUtil.sign(payload, device)

    DeviceStateUpdate(
      id = UUIDUtil.uuid,
      k = k,
      s = s,
      p = payload
    )
  }
}
