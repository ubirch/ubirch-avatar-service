package com.ubirch.keyservice

import java.security.{KeyPair, _}
import java.util.Base64

import com.typesafe.scalalogging.slf4j.StrictLogging
import net.i2p.crypto.eddsa.KeyPairGenerator
import net.i2p.crypto.eddsa.spec.{EdDSANamedCurveTable, EdDSAParameterSpec}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by derMicha on 24/04/17.
  */
object KeyServiceManager
  extends StrictLogging {

  private var keys: mutable.HashMap[String, KeyPair] = mutable.HashMap()

  def getKeyPairForDevice(deviceId: String)(implicit context: ExecutionContext): Future[Option[KeyPair]] = {
    keys.get(deviceId) match {
      case Some(keyPair) =>
        Future(Some(keyPair))
      case None =>
        val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.CURVE_ED25519_SHA512)
        val kpg: KeyPairGenerator = new KeyPairGenerator

        kpg.initialize(spec, new SecureRandom(java.util.UUID.randomUUID.toString.getBytes))

        val newKeyPair = kpg.generateKeyPair

        keys += (deviceId -> newKeyPair)
        Future(Some(newKeyPair))
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
    Base64.getEncoder.encodeToString(pubKey.getEncoded)
  }

}
