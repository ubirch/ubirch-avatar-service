package com.ubirch.services.util

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.idservice.client.IdServiceClientCached
import com.ubirch.util.crypto.ecc.EccUtil
import com.ubirch.util.crypto.hash.HashUtil
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
  private val eccUtil = new EccUtil()

  /**
    * @deprecated this code is legacy and will be deleted asap
    */
  def createSimpleSignature(payload: JValue, hwDeviceId: String): String = {

    val payloadString = Json4sUtil.jvalue2String(payload)
    val concatenated = s"$hwDeviceId$payloadString"

    HashUtil.sha512Base64(concatenated)
  }

  def validateSimpleMessage(hashedHwDeviceId: String): Future[Option[Device]] = {
    logger.info(s"validateSimpleMessage: device id=$hashedHwDeviceId")
    DeviceManager.infoByHashedHwId(hashedHwDeviceId).map {
      case Some(device) =>
        logger.debug(s"found device with primaryKey: $hashedHwDeviceId")
        Some(device)
      case None =>
        logger.error(s"device with primaryKey=$hashedHwDeviceId not found")
        None
    }
  }

  def validateMessage(hashedHwDeviceId: String, authToken: String, payload: JValue): Future[Option[Device]] = {
    logger.info("validateMessage")
    DeviceManager.infoByHashedHwId(hashedHwDeviceId).map {
      case Some(device) =>
        logger.debug(s"found device with primaryKey: $hashedHwDeviceId")
        val currentAuthToken = createSimpleSignature(payload, device)
        if (currentAuthToken == authToken) {
          Some(device)
        } else {
          logger.error(s"playload for device with primaryKey=$hashedHwDeviceId has invalid authToken (currentAuthToken: $currentAuthToken != authToken: $authToken ")
          None
        }
      case None =>
        logger.error(s"device with hashedHwDeviceId=$hashedHwDeviceId not found")
        None
    }
  }

  def validateSignedMessageWithKey(key: String,
                                   signature: String,
                                   payload: JValue): Future[Boolean] = {
    val binPayload = write(payload).getBytes

    validateSignedMessageWithKey(key = key,
      signature = signature,
      payload = binPayload
    )
  }

  def validateSignedMessageWithKey(key: String,
                                   signature: String,
                                   payload: Array[Byte]): Future[Boolean] = {

    Future(eccUtil.validateSignature(publicKey = key, signature = signature, payload = payload))
  }

  def validateSignedMessage(device: Device,
                            signature: String,
                            payload: JValue,
                            hashedPayload: Boolean
                           )
                           (implicit httpClient: HttpExt, materializer: Materializer, system: ActorSystem): Future[Boolean] = {
    val binData = write(payload).getBytes
    validateSignedMessage(device = device,
      signature = signature,
      payload = binData,
      hashedPayload = hashedPayload)
  }

  def validateSignedMessage(device: Device,
                            signature: String,
                            payload: Array[Byte],
                            hashedPayload: Boolean
                           )
                           (implicit httpClient: HttpExt, materializer: Materializer, system: ActorSystem): Future[Boolean] = {
    try {

      IdServiceClientCached.currentlyValidPubKeysCached(device.hwDeviceId.toLowerCase).map {
        case Some(keys) if keys.isEmpty =>
          //throw new Exception(s"device ${device.deviceId} has no aktive keys")
          logger.error(s"device ${device.deviceId} has no aktive keys")
          false
        case Some(keys) =>
          keys.map { key =>
            val valid = if (hashedPayload)
              try {
                eccUtil.validateSignatureSha512(publicKey = key.pubKeyInfo.pubKey, signature = signature, payload = payload)
              }
              catch {
                case e: Exception => false
              }
            else
              try {
                eccUtil.validateSignature(publicKey = key.pubKeyInfo.pubKey, signature = signature, payload = payload)
              }
              catch {
                case e: Exception => false
              }
            valid
          }.count(_ == true) == 1
        case None =>
          logger.error(s"no pubkeys found for deviceId: ${device.deviceId}")
          false
      }
    }
    catch {
      case e: Exception =>
        logger.error("Error while key lookup")
        Future(false)
    }
  }
}