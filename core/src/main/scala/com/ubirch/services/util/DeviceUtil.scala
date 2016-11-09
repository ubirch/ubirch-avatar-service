package com.ubirch.services.util

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.Device
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.json.Json4sUtil
import org.json4s.JsonDSL._
import org.json4s._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by derMicha on 02/11/16.
  */
object DeviceUtil extends LazyLogging {

  private def createSimpleSignature(payload: JValue, device: Device): String = {

    createSimpleSignature(payload, device.hwDeviceId)

  }

  private def createSimpleSignature(payload: JValue, hwDeviceId: String): String = {

    val payloadString = Json4sUtil.jvalue2String(payload)
    val sig = s"$hwDeviceId$payloadString" // TODO iirc it's more secure (cryptographically) to reverse the append order (hwDeviceId last)

    HashUtil.sha512Base64(sig)

  }

  def validateMessage(hwDeviceId: String, authToken: String, payload: JValue): Future[Option[Device]] = {
    logger.info("validateMessage")
    DeviceManager.infoByHwId(hwDeviceId).map {
      case Some(device) =>
        logger.debug(s"found device wir primaryKey: $hwDeviceId")
        val currentAuthToken = createSimpleSignature(payload, device)
        currentAuthToken == authToken match {
          case true =>
            Some(device)
          case _ =>
            logger.error(s"playload for device with primaryKey=$hwDeviceId has invalid authToken (currentAuthToken: $currentAuthToken != authToken: $authToken ")
            None
        }
      case None =>
        logger.error(s"device with primaryKey=$hwDeviceId not found")
        None
    }
  }

  def defaultProps(deviceTypeKey: String): JValue = {
    val props = deviceTypeKey match {
      case Const.LIGHTSLAMP =>
        Map[String, String]()
      case Const.LIGHTSSENSOR =>
        Map[String, String](
          Const.STOREDATA -> Const.BOOL_TRUE
        )
      case Const.ENVIRONMENTSENSOR =>
        Map[String, String](
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_TRUE
        )
      case _ => Map[String, String]()
    }
    Json4sUtil.any2jvalue(props)
  }

  def defaultTags(deviceTypeKey: String): Set[String] = {
    deviceTypeKey match {
      case Const.LIGHTSLAMP =>
        Set[String](
          Const.TAG_UBB0,
          Const.TAG_ACTOR,
          Const.TAG_BTCD
        )
      case Const.LIGHTSSENSOR =>
        Set[String](
          Const.TAG_UBB0,
          Const.TAG_SENSOR,
          Const.TAG_BTCD
        )
      case Const.ENVIRONMENTSENSOR =>
        Set[String](
          Const.TAG_UBB1,
          Const.TAG_SENSOR,
          Const.TAG_BTCD
        )
      case _ =>
        Set[String](
          Const.TAG_UBB1
        )
    }
  }

  /**
    *
    * @param deviceType
    * @return
    * s = sensitivität des Sensort
    * ir = irfrarot filter
    * bf = 0/1
    * i = update intervall
    */
  def defaultConf(deviceType: String): JValue = {
    val conf = deviceType match {
      case Const.LIGHTSSENSOR =>
        Map[String, Int](
          Const.CONF_INTERVALL -> (15 * 60),
          Const.CONF_SENSIVITY -> 0,
          Const.CONF_INFRARED -> 20
        )
      case Const.LIGHTSLAMP =>
        Map[String, Int](
          Const.CONF_INTERVALL -> (15 * 60),
          Const.CONF_BLINKING -> 0
        )
      case Const.ENVIRONMENTSENSOR =>
        Map[String, Int](
          Const.CONF_INTERVALL -> (15 * 60),
          Const.CONF_THRESHOLD -> 3600
        )
      case _ =>
        Map[String, Int](
          Const.CONF_INTERVALL -> (15 * 60)
        )
    }
    Json4sUtil.any2jvalue(conf)
  }
}