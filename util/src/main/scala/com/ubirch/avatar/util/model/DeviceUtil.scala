package com.ubirch.avatar.util.model

import java.security._
import java.util.Base64

import com.ubirch.avatar.model.device.Device
import com.ubirch.keyservice.KeyServiceManager
import com.ubirch.util.json.JsonFormats
import net.i2p.crypto.eddsa.spec.{EdDSANamedCurveTable, EdDSAParameterSpec}
import net.i2p.crypto.eddsa.{EdDSAEngine, KeyPairGenerator}
import org.json4s._
import org.json4s.native.Serialization._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
  * author: derMicha
  * since: 2016-11-02
  */
object DeviceUtil {

  implicit val formats: Formats = JsonFormats.default

  def sign(payload: JValue, device: Device)(implicit context: ExecutionContext): Option[(String, String)] = {

    //TODO add private key management!!!
    val sgr: Signature = new EdDSAEngine(MessageDigest.getInstance("SHA-512"))

    val kp = Await.result(KeyServiceManager.getKeyPairForDevice(device.deviceId), 5 seconds)

    kp match {
      case Some(keyPair) =>
        val sKey: PrivateKey = keyPair.getPrivate
        val pKey: PublicKey = keyPair.getPublic

        sgr.initSign(sKey)
        val payloadStr = write(payload)
        sgr.update(payloadStr.getBytes)
        val signature: Array[Byte] = sgr.sign

        Some((Base64.getEncoder.encodeToString(pKey.getEncoded),
          Base64.getEncoder.encodeToString(signature)))
      case None => None
    }
  }

  def createKeyPair: (PrivateKey, PublicKey) = {
    val sgr: Signature = new EdDSAEngine(MessageDigest.getInstance("SHA-512"))
    val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.CURVE_ED25519_SHA512)
    val kpg: KeyPairGenerator = new KeyPairGenerator

    kpg.initialize(spec, new SecureRandom(java.util.UUID.randomUUID.toString.getBytes))

    val kp: KeyPair = kpg.generateKeyPair

    val sKey: PrivateKey = kp.getPrivate
    val pKey: PublicKey = kp.getPublic
    (sKey, pKey)
  }
}
