package com.ubirch.server.util

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.crypto.ecc.EccUtil
import net.i2p.crypto.eddsa.{EdDSAPrivateKey, EdDSAPublicKey}
import org.apache.commons.codec.binary.Hex
import org.scalatest.{FeatureSpec, Matchers}

class ServerKeysSpec extends FeatureSpec
  with Matchers
  with StrictLogging {

  val data = "Hallo Welt"
  val dataBin: Array[Byte] = data.getBytes

  private val eccUtil = new EccUtil()


  feature("ServerKeys test") {

    scenario("sign data new keys") {
      val (puk, prk) = eccUtil.generateEccKeyPairEncoded

      val s = eccUtil.signPayload(prk, data)

      val v = eccUtil.validateSignature(puk, signature = s, payload = data)

      v shouldBe true
    }

    scenario("sign data new keys (2bin)") {
      val (puk, prk) = eccUtil.generateEccKeyPair

      val pukBin = EdDSAPublicKey.decode(puk.getEncoded)
      val prkBin = EdDSAPrivateKey.decode(prk.getEncoded)

      val pukBinHex = Hex.encodeHexString(pukBin)
      val prkBinHex = Hex.encodeHexString(prkBin)

      val puk2 = eccUtil.decodePublicKey(pukBin)
      val prk2 = eccUtil.decodePrivateKey(prkBin)

      puk2.equals(puk) shouldBe true
      prk2.equals(prk) shouldBe true

      val puk2Enc = eccUtil.encodePublicKey(puk2)
      val prk2Enc = eccUtil.encodePrivateKey(prk2)

      val s = eccUtil.signPayload(prk2Enc, data)
      val v = eccUtil.validateSignature(puk2Enc, signature = s, payload = data)

      true shouldBe true
    }


    scenario("sign data with Server Key") {
      val s = eccUtil.signPayload(ServerKeys.privKeyB64, data)

      val v = eccUtil.validateSignature(ServerKeys.pubKeyB64, signature = s, payload = data)

      v shouldBe true
    }

  }

}
