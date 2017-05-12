package com.ubirch.avatar.util.model

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.rest.device.Device
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
        //"""{"a":"b"}""".stripMargin
        //"""{"t":2833,"p":1011,"h":3078,"a":2588293,"la":"52.481083","lo":"13.3643","ba":100,"lp":13,"e":8,"aq":111,"aqr":121},{"t":2833,"p":1011,"h":3078,"a":2588293,"la":"52.481083","lo":"13.3643","ba":100,"lp":14,"e":0,"aq":111,"aqr":121},{"t":2833,"p":1011,"h":3078,"a":2588293,"la":"52.481083","lo":"13.3643","ba":100,"lp":15,"e":0,"aq":111,"aqr":121},{"t":2833,"p":1011,"h":3078,"a":2588293,"la":"52.481083","lo":"13.3643","ba":100,"lp":16,"e":0,"aq":111,"aqr":121},{"t":2832,"p":1011,"h":3073,"a":2588293,"la":"52.481083","lo":"13.3643","ba":100,"lp":17,"e":0,"aq":111,"aqr":121}""".stripMargin
        """[{"t":2630,"p":1014,"h":2783,"a":2587090,"la":"52.481083","lo":"13.3643","ba":100,"lp":1,"e":0,"aq":111,"aqr":121,"ts":"2004-1-1/0:0:32"},{"t":2630,"p":1014,"h":2783,"a":2587090,"la":"52.481083","lo":"13.3643","ba":100,"lp":2,"e":0,"aq":111,"aqr":121,"ts":"2004-01-01 00:00:33"}]"""
      )

      val (k, s) = DeviceUtil.sign(payload, device)
      //      val k = "MC0wCAYDK2VkCgEBAyEAh81rIyZtMc4Yb1ECUoFrxq+/NNaY9Tm3+Mc4xroauJw="
      //       val s = "TD2MXIwhycXRwWlAvGIkr7nSBMb5ctzhwzMJoot3rB6PxMg7gACioX8QfUXSDC2nSiOwsrUyNeCTcZfeZRpYCQ=="
      //      val k = "OIX0hvZpHPKiWQRgOFhA6oYOMK6mT6mldtoyg578cPs="
      //      val s = "3uIlW9gtxs0mhc+Y2FI4YY8qJsMGdWgH7Cq/Fjzg26/rjAu098LFQLsLjJyB\noEiBmLSSEMJCu3VrH5TDHVO1Ag=="

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
