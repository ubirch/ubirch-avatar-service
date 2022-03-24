package com.ubirch.services.util

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.db.device.Device
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


}