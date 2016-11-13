package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.{DeviceDataRawManager, DeviceManager}
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.avatar.util.model.StorageCleanup
import com.ubirch.services.util.DeviceUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.RequestBody

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

/**
  * Created by derMicha on 09/11/16.
  */
object InitData extends App with StrictLogging with StorageCleanup {

  //cleanElasticsearch
  val httpClient = new HttpClient
  val avatarServiceUrl = "http://localhost:8080/api/avatarService/v1/device/update"

  Random.setSeed(DateTime.now.getMillisOfDay)

  val device = Device(
    deviceId = UUIDUtil.uuidStr,
    deviceName = "testHans001",
    hwDeviceId = UUIDUtil.uuidStr,
    deviceTypeKey = Const.ENVIRONMENTSENSOR
  )

  val now = DateTime.now()

  //  Await.result(DeviceManager.createWithShadow(device), 5 seconds) match {
  Await.result(DeviceManager.create(device), 5 seconds) match {
    case Some(dev) =>
      logger.info(s"created: $dev")

      (1 to 50).foreach { i =>

        val t = 2000 + Random.nextInt(1500)
        val p = 90000 + Random.nextInt(20000)
        val h = 4000 + Random.nextInt(5500)

        val payload =
          s"""
             |[
             |{
             |"t":$t,
             |"p":$p,
             |"h":$h
             |}
             |]
        """.stripMargin
        val payLoadJson = Json4sUtil.string2JValue(payload).get
        val (k, s) = DeviceUtil.sign(payLoadJson, dev)
        val ddr = DeviceDataRaw(
          a = dev.hashedHwDeviceId,
          k = Some(k),
          s = s,
          ts = now.minusMinutes(i * 5),
          p = payLoadJson
        )

        val msg = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(ddr).get)
        logger.info(s"msg: $msg")

        val ddrString = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(ddr).get)
        val body = RequestBody(ddrString, APPLICATION_JSON)
        //httpClient.post(new URL(avatarServiceUrl), Some(body))

        DeviceDataRawManager.store(ddr)
      }
    case None =>
      logger.error("device could not be created")
  }
}
