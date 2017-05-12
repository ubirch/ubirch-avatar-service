package com.ubirch.avatar.transformer.services

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.{Device, DeviceDataRaw, DeviceType}
import com.ubirch.avatar.model.rest.payload.{TrackleSensorPayload, TrackleSensorPayloadOut}
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.transformer.services.TransformerService
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.json4s.JValue
import org.scalactic.TolerantNumerics
import org.scalatest.{FeatureSpec, Matchers}

import spire.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by derMicha on 30/11/16.
  */
class TransformerServiceTrackleSensorTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  val deviceTypeTrackleSensor: DeviceType = DeviceTypeUtil.defaultDeviceType(deviceType = Const.TRACKLESENSOR)

  val ddrTrackleSensorStr: String =
    s"""{
       |  "id": "600160fc-f1fa-456e-9c1f-2c1c4460b9c3",
       |  "v": "${MessageVersion.v002}",
       |  "a": "Tf9Oo0DwqCPxXT9PAati6uDl2lecy4Ufjbnf6ExYsrN7iZA6dA4e4XLaeTpuedVg5ff5vQWKEqKAQz7W+kZRCg==",
       |  "k": "ltFWVRo3/gzF++Hv0XScx8uVYd078cJhQwssLarnseY=",
       |  "ts": "2017-01-14T13:44:19.451Z",
       |  "s": "V3EbZMqK0pISsmuWbqWJMaT20A1YJ+lDakoMgdTmCRBgQpWMHYYNzDgdCurunSJXqF0KbkPc0XubmdS3AR/PBQ==",
       |  "p": {
       |    "ts": 261,
       |    "pc": 215,
       |    "t1": 22187,
       |    "t2": 22199,
       |    "t3": 22206,
       |    "st": 64,
       |    "vb": 2263,
       |    "rs": 19,
       |    "la": "52.487202",
       |    "lo": "13.463557",
       |    "ba": 100,
       |    "e": 0
       |  }
       |}
       |""".stripMargin

  val ddrTrackleSensorJVal: JValue = Json4sUtil.string2JValue(ddrTrackleSensorStr).get

  lazy val ddrTrackleSensor: DeviceDataRaw = ddrTrackleSensorJVal.extract[DeviceDataRaw]

  lazy val device: Device = DummyDevices.device(
    deviceTypeKey = deviceTypeTrackleSensor.key
  )

  lazy val payload: TrackleSensorPayload = ddrTrackleSensor.p.extract[TrackleSensorPayload]

  feature("transform services for envSensor") {
    scenario("convert incoming data") {

      val trd = TransformerService.transform(
        device = device,
        deviceType = deviceTypeTrackleSensor,
        drd = ddrTrackleSensor,
        sdrd = ddrTrackleSensor
      )

      trd.isDefined shouldBe true
      val tPayload = trd.get.deviceMessage.extractOpt[TrackleSensorPayloadOut]

      implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(2)

      tPayload.isDefined shouldBe true
      tPayload.get.t1.toBigDecimal.setScale(2, BigDecimal.RoundingMode.HALF_UP) shouldBe 36.68
      tPayload.get.t2.toBigDecimal.setScale(2, BigDecimal.RoundingMode.HALF_UP) shouldBe 36.84
      tPayload.get.t3.toBigDecimal.setScale(2, BigDecimal.RoundingMode.HALF_UP) shouldBe 36.94
    }
  }
}
