package com.ubirch.avatar.util.model

import java.security.{KeyPair, MessageDigest, PrivateKey, PublicKey, SecureRandom, Signature}
import java.util.Base64

import com.ubirch.avatar.model.device.Device

import org.json4s._
import org.json4s.native.Serialization._

import net.i2p.crypto.eddsa.spec.{EdDSANamedCurveTable, EdDSAParameterSpec}
import net.i2p.crypto.eddsa.{EdDSAEngine, KeyPairGenerator}

/**
  * author: derMicha
  * since: 2016-11-02
  */
object DeviceUtil {

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

}
