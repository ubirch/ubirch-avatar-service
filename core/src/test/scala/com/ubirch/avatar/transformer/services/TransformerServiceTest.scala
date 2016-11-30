package com.ubirch.avatar.transformer.services

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.device.{DeviceDataRaw, EnvSensorPayload, EnvSensorRawPayload}
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.transformer.services.TransformerService
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.scalatest.{FeatureSpec, Matchers}

/**
  * Created by derMicha on 30/11/16.
  */
class TransformerServiceTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  val deviceType = DeviceTypeUtil.defaultDeviceType(deviceType = "envSensor")

  val ddrEnvSensorStr =
    s"""{
       |    "id": "46d29ab8-b772-46b7-8432-4843b96d3c1f",
       |    "v": "0.0.2",
       |    "a": "2d6+bRgFyJata8W++TsJqAcSjg5FWHvKvfFiwVEXOoMIpui/1Wwsqp+krybl+ERPGqvP1W4YZuWnQeC5w5B56Q==",
       |    "k": "pb37Vbe8tpo0+ed4nO/emQryVAMfAkIjZWFtWLiGbmA=",
       |    "ts": "2016-11-29T21:21:21.418Z",
       |    "s": "BqhYuOtvTuMK9Xj5SUdZepeZei9dIc5yjLII1SbbRk/P38SZP74KxG4AlbOTSj20x/HfFjcGvw0rjYdGz+sWCQ==",
       |    "p": {
       |      "t": 1861,
       |      "p": 102787,
       |      "h": 3939,
       |      "a": -12101,
       |      "la": "52.503067",
       |      "lo": "13.479367",
       |      "ba": 100,
       |      "lp": 0,
       |      "e": 0
       |    }
       |}
       |""".stripMargin

  val ddrEnvSensorJVal = Json4sUtil.string2JValue(ddrEnvSensorStr).get

  lazy val ddrEnvSensor = ddrEnvSensorJVal.extract[DeviceDataRaw]

  lazy val device = DummyDevices.device(
    deviceTypeKey = deviceType.key
  )

  lazy val payload = ddrEnvSensor.p.extract[EnvSensorRawPayload]

  feature("transform services for envSensor") {
    scenario("convert incoming data") {

      val trd = TransformerService.transform(
        device = device,
        deviceType = deviceType,
        drd = ddrEnvSensor,
        sdrd = ddrEnvSensor
      )

      val tPayload = trd.deviceMessage.extractOpt[EnvSensorPayload]

      tPayload.isDefined shouldBe true
      tPayload.get.temperature shouldBe (payload.t / 100)
      tPayload.get.humidity shouldBe (payload.h / 100)
      tPayload.get.presure shouldBe (payload.p / 100)

    }
  }

}
