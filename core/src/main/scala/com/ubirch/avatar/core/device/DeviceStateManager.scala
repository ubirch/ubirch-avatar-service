package com.ubirch.avatar.core.device

import java.security._
import java.util.Base64

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.awsiot.services.AwsShadowService
import com.ubirch.avatar.model.device.{Device, DeviceStateUpdate}
import com.ubirch.services.util.DeviceUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil
import net.i2p.crypto.eddsa.spec.{EdDSANamedCurveTable, EdDSAParameterSpec}
import net.i2p.crypto.eddsa.{EdDSAEngine, KeyPairGenerator}
import org.joda.time.DateTime
import org.json4s._
import org.json4s.native.Serialization._

/**
  * Created by derMicha on 09/11/16.
  */
object DeviceStateManager extends MyJsonProtocol with LazyLogging {

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
