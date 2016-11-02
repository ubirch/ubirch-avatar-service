package com.ubirch.services.util

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.Device
import com.ubirch.util.json.Json4sUtil
import org.json4s._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by derMicha on 02/11/16.
  */
object DeviceUtil extends LazyLogging {

  def createSimpleSignature(payload: JValue, device: Device): String = {

    createSimpleSignature(payload, device.hwDeviceId)

  }

  def createSimpleSignature(payload: JValue, hwDeviceId: String): String = {

    val payloadString = Json4sUtil.jvalue2String(payload)

    val sig = s"$hwDeviceId$payloadString"

    SimpleHashUtil.hashString512B64(sig)
  }

  def validateMessage(primaryKey: String, authToken: String, payload: JValue): Future[Option[Device]] = {
    logger.info("validateMessage")
    DeviceManager.info(primaryKey).map {
      case Some(device) =>
        logger.debug(s"found device wir primaryKey: $primaryKey")
        val currentAuthToken = createSimpleSignature(payload, device)
        currentAuthToken == authToken match {
          case true =>
            Some(device)
          case _ =>
            logger.error(s"playload for device with primaryKey=$primaryKey has invalid authToken (currentAuthToken: $currentAuthToken != authToken: $authToken ")
            None
        }
      case None =>
        logger.error(s"device with primaryKey=$primaryKey not found")
        None
    }
  }
}