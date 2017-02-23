package com.ubirch.avatar.transformer.services

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw, DeviceType}
import com.ubirch.avatar.model.payload.{EnvSensorPayload, EnvSensorRawPayload}
import com.ubirch.avatar.model.{DummyDevices, MessageVersion}
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.transformer.services.TransformerService
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.json4s.JValue
import org.scalatest.{FeatureSpec, Matchers}

/**
  * Created by derMicha on 30/11/16.
  */
class TransformerServiceEnvSensorTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  val deviceTypeEnvSensor: DeviceType = DeviceTypeUtil.defaultDeviceType(deviceType = Const.ENVIRONMENTSENSOR)

  val ddrEnvSensorStr: String =
    s"""{
       |    "id": "46d29ab8-b772-46b7-8432-4843b96d3c1f",
       |    "v": "${MessageVersion.v002}",
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

  val ddrEnvSensorJVal: JValue = Json4sUtil.string2JValue(ddrEnvSensorStr).get

  lazy val ddrEnvSensor: DeviceDataRaw = ddrEnvSensorJVal.extract[DeviceDataRaw]

  lazy val device: Device = DummyDevices.device(
    deviceTypeKey = deviceTypeEnvSensor.key
  )

  lazy val payload: EnvSensorRawPayload = ddrEnvSensor.p.extract[EnvSensorRawPayload]

  feature("transform services for envSensor") {
    scenario("convert incoming data") {

      val trd = TransformerService.transform(
        device = device,
        deviceType = deviceTypeEnvSensor,
        drd = ddrEnvSensor,
        sdrd = ddrEnvSensor
      )

      val tPayload = trd.deviceMessage.extractOpt[EnvSensorPayload]

      tPayload.isDefined shouldBe true
      tPayload.get.temperature shouldBe (payload.t.toDouble / 100)
      tPayload.get.humidity shouldBe (payload.h.toDouble / 100)
      tPayload.get.presure shouldBe (payload.p.toDouble / 100)

    }
  }

}