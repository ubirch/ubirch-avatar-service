package com.ubirch.keyservice

import java.security.{KeyPair, _}
import java.util.Base64

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.device.Device
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import net.i2p.crypto.eddsa.spec.{EdDSANamedCurveTable, EdDSAParameterSpec}
import net.i2p.crypto.eddsa.{EdDSAEngine, KeyPairGenerator}
import org.json4s.JValue

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by derMicha on 24/04/17.
  */
object KeyServiceManager
  extends StrictLogging
    with MyJsonProtocol {

  private var keys: mutable.HashMap[String, KeyPair] = mutable.HashMap()

  def getKeyPairForDevice(deviceId: String)(implicit context: ExecutionContext): Future[Option[KeyPair]] = {
    keys.get(deviceId) match {
      case Some(keyPair) =>
        Future(Some(keyPair))
      case None =>

        val newKeyPair = createKeyPair

        keys += (deviceId -> newKeyPair)
        Future(Some(newKeyPair))
    }
  }

  def createKeyPair: KeyPair = {
    val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.CURVE_ED25519_SHA512)
    val kpg: KeyPairGenerator = new KeyPairGenerator

    kpg.initialize(spec, new SecureRandom(java.util.UUID.randomUUID.toString.getBytes))

    val kp: KeyPair = kpg.generateKeyPair

    kp
  }

  def sign(payload: JValue, device: Device)(implicit context: ExecutionContext): Option[(String, String)] = {

    //TODO add private key management!!!
    val sgr: Signature = new EdDSAEngine(MessageDigest.getInstance("SHA-512"))

    val kp = Await.result(KeyServiceManager.getKeyPairForDevice(device.deviceId), 5 seconds)

    kp match {
      case Some(keyPair) =>
        val sKey: PrivateKey = keyPair.getPrivate
        val pKey: PublicKey = keyPair.getPublic

        sgr.initSign(sKey)
        val payloadStr = Json4sUtil.jvalue2String(payload)
        sgr.update(payloadStr.getBytes)
        val signature: Array[Byte] = sgr.sign

        Some((Base64.getEncoder.encodeToString(pKey.getEncoded),
          Base64.getEncoder.encodeToString(signature)))
      case None => None
    }
  }

  def getEncodedPubKeyForDevice(deviceId: String)(implicit context: ExecutionContext): Future[Option[String]] = {

    getKeyPairForDevice(deviceId) map {
      case Some(kp) =>
        val pubKey = kp.getPublic
        val encodedPubKey = encodePubKey(pubKey)
        Some(encodedPubKey)
      case None => None
    }
  }

  def encodePubKey(pubKey: PublicKey): String = {
    encode(pubKey.getEncoded)
  }

  def encodePrivateKey(privateKey: PrivateKey): String = {
    encode(privateKey.getEncoded)
  }

  private def encode(data: Array[Byte]): String = {
    Base64.getEncoder.encodeToString(data)
  }
}
