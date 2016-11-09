package com.ubirch.avatar

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.Device
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by derMicha on 09/11/16.
  */
object InitData extends App with LazyLogging {

  val device = Device(
    deviceId = UUIDUtil.uuidStr,
    deviceName = "testHans001"
  )

  DeviceManager.create(device).map { d =>
    logger.info(s"created: $d")
  }
}
