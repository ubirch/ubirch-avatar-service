package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.services.util.DeviceUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 09/11/16.
  */
object InitData extends App with LazyLogging {

  val device = Device(
    deviceId = UUIDUtil.uuidStr,
    deviceName = "testHans001",
    hwDeviceId = UUIDUtil.uuidStr
  )

  Await.result(DeviceManager.createWithShadow(device), 5 seconds) match {
    case Some(dev) =>
      logger.info(s"created: $dev")
      val payload =
        """
          |{
          |"a":"b"
          |}
        """.stripMargin
      val payLoadJson = Json4sUtil.string2JValue(payload).get
      val (k, s) = DeviceUtil.sign(payLoadJson, dev)
      val rawMsg = DeviceDataRaw(
        a = dev.hashedHwDeviceId,
        k = Some(k),
        s = s,
        p = payLoadJson
      )
      val msg = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(rawMsg).get)
      logger.info(s"msg: $msg")
    case None =>
      logger.error("device could not be created")
  }
}
