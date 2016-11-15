package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.client.rest.AvatarRestClient
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.avatar.util.model.StorageCleanup

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 09/11/16.
  */
object InitData extends App with StrictLogging with StorageCleanup {

  cleanElasticsearch()

  val device = DummyDevices.device(deviceTypeKey = Const.ENVIRONMENTSENSOR)

  Await.result(DeviceManager.createWithShadow(device), 5 seconds) match {
    case Some(dev) =>

      logger.info(s"created: $dev")

      val (_, series) = DummyDeviceDataRaw.dataSeries(
        device = device,
        elementCount = 50,
        intervalMillis = 1000 * 60 * 5, // 5 mins
        timestampOffset = 0
      )()

      series foreach { dataRaw =>
        AvatarRestClient.deviceUpdate(dataRaw)
        Thread.sleep(500)
      }

    case None =>
      logger.error("device could not be created")
  }

}
