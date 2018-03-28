package com.ubirch.avatar.util.server

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.crypto.ecc.EccUtil
import net.i2p.crypto.eddsa.{EdDSAPrivateKey, EdDSAPublicKey}
import org.apache.commons.codec.binary.Hex
import org.scalatest.{FeatureSpec, Matchers}

class ServerKeysSpec extends FeatureSpec
  with Matchers
  with StrictLogging {

  val data = "Hallo Welt"
  val dataBin = data.getBytes

  feature("ServerKeys test") {

    scenario("sign data new keys") {
      val (puk, prk) = EccUtil.generateEccKeyPairEncoded

      val s = EccUtil.signPayload(prk, data)

      val v = EccUtil.validateSignature(puk, signature = s, payload = data)

      v shouldBe true
    }

    scenario("sign data new keys (2bin)") {
      val (puk, prk) = EccUtil.generateEccKeyPair

      val pukBin = EdDSAPublicKey.decode(puk.getEncoded)
      val prkBin = EdDSAPrivateKey.decode(prk.getEncoded)

      val pukBinHex = Hex.encodeHexString(pukBin)
      val prkBinHex = Hex.encodeHexString(prkBin)

      val puk2 = EccUtil.decodePublicKey(pukBin)
      val prk2 = EccUtil.decodePrivateKey(prkBin)

      puk2.equals(puk) shouldBe true
      prk2.equals(prk) shouldBe true

      val puk2Enc = EccUtil.encodePublicKey(puk2)
      val prk2Enc = EccUtil.encodePrivateKey(prk2)

      val s = EccUtil.signPayload(prk2Enc, data)
      val v = EccUtil.validateSignature(puk2Enc, signature = s, payload = data)

      true shouldBe true
    }


    scenario("sign data with Server Key") {
      val s = EccUtil.signPayload(ServerKeys.privKeyEnc, data)

      val v = EccUtil.validateSignature(ServerKeys.pubKeyEnc, signature = s, payload = data)

      v shouldBe true
    }

  }

}
