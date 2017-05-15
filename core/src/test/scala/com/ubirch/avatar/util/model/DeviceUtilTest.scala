package com.ubirch.avatar.util.model

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.util.json.MyJsonProtocol

import org.json4s.JValue
import org.json4s.native.Serialization.read
import org.scalatest.{FeatureSpec, Matchers}

/**
  * Created by derMicha on 18/01/17.
  */
class DeviceUtilTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  val device: Device = DummyDevices.device1

  feature("DeviceUtil") {

    scenario("sign message") {

      val payload = read[JValue](
        """[{"t":2630,"p":1014,"h":2783,"a":2587090,"la":"52.481083","lo":"13.3643","ba":100,"lp":1,"e":0,"aq":111,"aqr":121,"ts":"2004-1-1/0:0:32"},{"t":2630,"p":1014,"h":2783,"a":2587090,"la":"52.481083","lo":"13.3643","ba":100,"lp":2,"e":0,"aq":111,"aqr":121,"ts":"2004-01-01 00:00:33"}]"""
      )

      val (k, s) = DeviceUtil.sign(payload, device)

      logger.info(s"k: $k")
      logger.info(s"s: $s")

      val checkedD = DeviceCoreUtil.validateSignedMessage(device.hwDeviceId, k, s, payload)
      checkedD shouldBe true
    }

    scenario("sign empty message") {

      val payload = read[JValue]("")

      val (k, s) = DeviceUtil.sign(payload, device)

      val checkedD = DeviceCoreUtil.validateSignedMessage(device.hwDeviceId, k, s, payload)
      checkedD shouldBe true

    }

  }

}
