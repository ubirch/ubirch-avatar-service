package com.ubirch.avatar.cmd

import java.net.URL

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.avatar.util.model.StorageCleanup
import com.ubirch.util.json.Json4sUtil

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

  cleanElasticsearch()
  val httpClient = new HttpClient
  val avatarServiceUrl = "http://localhost:8080/api/avatarService/v1/device/update"

  Random.setSeed(DateTime.now.getMillisOfDay)

  val device = DummyDevices.device(deviceTypeKey = Const.ENVIRONMENTSENSOR)

  val now = DateTime.now()

  Await.result(DeviceManager.createWithShadow(device), 5 seconds) match {
    case Some(dev) =>

      logger.info(s"created: $dev")

      val (_, series) = DummyDeviceDataRaw.dataSeries(device = device,
        elementCount = 50,
        intervalMillis = 1000 * 60 * 5, // 5 mins
        timestampOffset = 0
      )()

      series foreach { dataRaw =>

        val msg = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(dataRaw).get)
        logger.info(s"msg: $msg")

        val body = RequestBody(msg, APPLICATION_JSON)
        httpClient.post(new URL(avatarServiceUrl), Some(body))
        Thread.sleep(500)

      }

    case None =>
      logger.error("device could not be created")
  }
}
