package com.ubirch.keyservice

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.device.Device
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.util.json.MyJsonProtocol
import org.json4s.JValue
import org.json4s.native.Serialization.read
import org.scalatest.{FeatureSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by derMicha on 18/01/17.
  */
//@TODO this test is placed in the wrong module because of f*cking dependencies
class KeyServiceManagerSpec
  extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  val device: Device = DummyDevices.device1

  feature("DeviceUtil") {

    scenario("sign message") {

      val payload = read[JValue](
        """{"a":"b"}""".stripMargin
      )

      KeyServiceManager.sign(payload, device) match {
        case Some((k, s)) =>
          val checkedD = DeviceCoreUtil.validateSignedMessage(device.hwDeviceId, k, s, payload)
          checkedD shouldBe true
        case None =>
          fail("could not sign")
      }
    }

    scenario("sign empty message") {

      val payload = read[JValue]("")

      KeyServiceManager.sign(payload, device) match {
        case Some((k, s)) =>
          val checkedD = DeviceCoreUtil.validateSignedMessage(device.hwDeviceId, k, s, payload)
          checkedD shouldBe true
        case None =>
          fail("could not sign")
      }

    }

  }
}
