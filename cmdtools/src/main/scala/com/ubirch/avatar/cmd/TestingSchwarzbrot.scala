package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.model.rest.payload.EnvSensorRawPayload
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

/**
  * Created by derMicha on 18/05/17.
  */
object TestingSchwarzbrot extends App with MyJsonProtocol with StrictLogging {

  val string =
    """
      |{
      |  "t": 2169,
      |  "p": 105681,
      |  "h": 7697,
      |  "la": "52.5119523",
      |  "lo": "13.2116310",
      |  "a": 14326,
      |  "ts": "2017-05-18T15:26:18.502Z"
      |}
    """.stripMargin

  val string2 =
    """
      |  {
      |  "t": 2225,
      |  "p": 108681,
      |  "h": 8852,
      |  "la": "52.5111521",
      |  "lo": "13.2112572",
      |  "a": 6664,
      |  "ts": "2017-05-18T16:57:17.133+02:00"
      |}
    """.stripMargin

  val json = Json4sUtil.string2JValue(string2).get

  val envRaw = json.extract[EnvSensorRawPayload]

  println(envRaw.ts)
}
