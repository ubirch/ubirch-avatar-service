package com.ubirch.services.util

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.crypto.ecc.EccUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.json4s._
import org.json4s.native.Serialization._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by derMicha on 02/11/16.
  */
object DeviceCoreUtil extends MyJsonProtocol with StrictLogging {

  private def createSimpleSignature(payload: JValue, device: Device): String = createSimpleSignature(payload, device.hwDeviceId)

  /**
    * @deprecated this code is legacy and will be deleted asap
    */
  def createSimpleSignature(payload: JValue, hwDeviceId: String): String = {

    val payloadString = Json4sUtil.jvalue2String(payload)
    val concatenated = s"$hwDeviceId$payloadString"

    HashUtil.sha512Base64(concatenated)
  }

  def validateSimpleMessage(hwDeviceId: String): Future[Option[Device]] = {
    logger.info("validateSimpleMessage")
    DeviceManager.infoByHashedHwId(hwDeviceId).map {
      case Some(device) =>
        logger.debug(s"found device with primaryKey: $hwDeviceId")
        Some(device)
      case None =>
        logger.error(s"device with primaryKey=$hwDeviceId not found")
        None
    }
  }

  def validateMessage(hwDeviceId: String, authToken: String, payload: JValue): Future[Option[Device]] = {
    logger.info("validateMessage")
    DeviceManager.infoByHashedHwId(hwDeviceId).map {
      case Some(device) =>
        logger.debug(s"found device with primaryKey: $hwDeviceId")
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

  def validateSignedMessage(key: String, signature: String, payload: JValue): Boolean = {

    val payloadString = write(payload)
    EccUtil.validateSignature(publicKey = key, signature = signature, payload = payloadString)

  }

  /**
    * checks whether notary service should be used for this device
    *
    * @param device the device on which to to perform the check
    * @return
    */
  def checkNotaryUsage(device: Device): Boolean = {
    if (device.deviceProperties.isDefined) {
      (device.deviceProperties.get \ Const.BLOCKC).extractOpt[String].getOrElse(Const.BOOL_FALSE).toLowerCase match {
        case Const.BOOL_TRUE =>
          true
        case _ =>
          false
      }
    }
    else
      false
  }
}