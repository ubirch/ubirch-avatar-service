package com.ubirch.services.util

import java.security._
import java.util.Base64

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.{Device, DeviceType, DeviceTypeDefaults, DeviceTypeName}
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import net.i2p.crypto.eddsa.spec.{EdDSANamedCurveTable, EdDSAParameterSpec, EdDSAPublicKeySpec}
import net.i2p.crypto.eddsa.{EdDSAEngine, EdDSAPublicKey, KeyPairGenerator}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by derMicha on 02/11/16.
  */
object DeviceUtil extends MyJsonProtocol with LazyLogging {

  private def createSimpleSignature(payload: JValue, device: Device): String = {

    createSimpleSignature(payload, device.hwDeviceId)

  }

  /**
    * @deprecated this code is legacy and will be deleted asap
    */
  private def createSimpleSignature(payload: JValue, hwDeviceId: String): String = {

    val payloadString = Json4sUtil.jvalue2String(payload)
    val concatenated = s"$hwDeviceId$payloadString"

    HashUtil.sha512Base64(concatenated)
  }

  def validateMessage(hwDeviceId: String, authToken: String, payload: JValue): Future[Option[Device]] = {
    logger.info("validateMessage")
    DeviceManager.infoByHashedHwId(hwDeviceId).map {
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

  def validateSignedMessage(hashedHwDeviceId: String, key: String, signature: String, payload: JValue): Future[Option[Device]] = {
    DeviceManager.infoByHashedHwId(hashedHwDeviceId).map {
      case Some(device: Device) =>
        val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName("ed25519-sha-512")

        val decoded = Base64.getDecoder.decode(key)
        val pubKeyBytes: Array[Byte] = decoded.length match {
          case 32 => decoded
          case _ => EdDSAPublicKey.decode(decoded)
        }
        val pubKey: EdDSAPublicKeySpec = new EdDSAPublicKeySpec(pubKeyBytes, spec)
        val pKey: PublicKey = new EdDSAPublicKey(pubKey)

        val signatureByte: Array[Byte] = Base64.getDecoder.decode(signature)
        val sgr: EdDSAEngine = new EdDSAEngine(MessageDigest.getInstance("SHA-512"))

        sgr.initVerify(pKey)
        sgr.update(write(payload).getBytes())
        sgr.verify(signatureByte) match {
          case true =>
            Some(device)
          case _ =>
            None
        }
      case None =>
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
    * The following fields can be included in default configs:
    *
    * * s = sensor sensitivity
    * * ir = infrared filter
    * * bf = 0/1
    * * i = update interval
    *
    * @param deviceType device type the default config applies to
    * @return default config for the given device type
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

  def sign(payload: JValue, device: Device): (String, String) = {
    //TODO add private key management!!!
    val sgr: Signature = new EdDSAEngine(MessageDigest.getInstance("SHA-512"))
    val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.CURVE_ED25519_SHA512)
    val kpg: KeyPairGenerator = new KeyPairGenerator

    kpg.initialize(spec, new SecureRandom(java.util.UUID.randomUUID.toString.getBytes))

    val kp: KeyPair = kpg.generateKeyPair

    val sKey: PrivateKey = kp.getPrivate
    val pKey: PublicKey = kp.getPublic

    sgr.initSign(sKey)
    sgr.update(write(payload).getBytes)
    val signature: Array[Byte] = sgr.sign

    (Base64.getEncoder.encodeToString(pKey.getEncoded),
      Base64.getEncoder.encodeToString(signature))
  }

  val defaultDeviceTypesSet: Set[String] = Set(Const.LIGHTSSENSOR, Const.LIGHTSLAMP, Const.ENVIRONMENTSENSOR)

  def defaultTranslation(deviceType: String): DeviceTypeName = {

    deviceType match {
      case Const.LIGHTSSENSOR =>
        DeviceTypeName("Lichtsensor", "Light Sensor")
      case Const.LIGHTSLAMP =>
        DeviceTypeName("Lampe", "Lamp")
      case Const.ENVIRONMENTSENSOR =>
        DeviceTypeName("Umweltsensor", "Environment Sensor")
      case _ =>
        DeviceTypeName("Unbekanntes Gerät", "Unknown Device")
    }

  }

  def defaultDeviceTypes: Set[DeviceType] = defaultDeviceTypesSet map defaultDeviceType

  def defaultDeviceType(deviceType: String): DeviceType = {
    DeviceType(
      key = deviceType,
      name = defaultTranslation(deviceType),
      icon = deviceType,
      transformerQueue = Some(s"ubirch.transformer.${deviceType.toLowerCase.trim}"),
      defaults = DeviceTypeDefaults(
        defaultProps(deviceType),
        defaultConf(deviceType),
        defaultTags(deviceType)
      )
    )
  }

  def defaultDeviceType(): DeviceType = {
    DeviceType(
      key = "defaultDeviceType",
      name = defaultTranslation("defaultDeviceType"),
      icon = "defaultDeviceType",
      transformerQueue = Some(s"ubirch.transformer.defaultDeviceType"),
      defaults = DeviceTypeDefaults(
        defaultProps("defaultDeviceType"),
        defaultConf("defaultDeviceType"),
        defaultTags("defaultDeviceType")
      )
    )
  }

}