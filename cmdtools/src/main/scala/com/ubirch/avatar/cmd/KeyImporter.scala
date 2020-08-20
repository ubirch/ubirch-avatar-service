package com.ubirch.avatar.cmd

import java.io.{File, FileInputStream}
import java.util.Base64

import com.google.common.io.ByteStreams
import com.ubirch.util.crypto.ecc.EccUtil
import org.apache.commons.codec.binary.Hex

object KeyImporter extends App {

  private val eccUtil = new EccUtil()
  val (pu2, pr2) = eccUtil.generateEccKeyPair

  val pu2Hex = Hex.encodeHexString(pu2.getEncoded)
  val pr2Hex = Hex.encodeHexString(pr2.getEncoded)

  val rawPubKeyFilename = "/Volumes/ubirch master keys/trackle/dev-master-ed25519.pub"
  val rawPrivKeyFilename = "/Volumes/ubirch master keys/trackle/dev-master-ed25519.bin"
  //  val rawPrivKeyFilename = "/Volumes/ubirch master keys/ubirch/20180328_local-backend-ed25519.prk.bin"

  val puBytes = ByteStreams.toByteArray(new FileInputStream(new File(rawPubKeyFilename)))
  val prBytes = ByteStreams.toByteArray(new FileInputStream(new File(rawPrivKeyFilename)))
  //val puBytes = prBytes.takeRight(32)

  val puBytesHex = Hex.encodeHexString(puBytes)
  val prBytesHex = Hex.encodeHexString(prBytes)

  val puBytes64 = Base64.getEncoder.encodeToString(puBytes)
  val prBytes64 = Base64.getEncoder.encodeToString(prBytes)

  val pu = eccUtil.decodePublicKey(puBytes)
  val pr = eccUtil.decodePrivateKey(prBytes)

  val pu3 = eccUtil.decodePublicKey(puBytesHex)
  val pr3 = eccUtil.decodePrivateKey(prBytesHex)

  val pu4 = eccUtil.decodePublicKey(puBytes64)
  val pr4 = eccUtil.decodePrivateKey(prBytes64)

  val puBytes2 = pu2.getEncoded
  val prBytes2 = pr2.getEncoded

  println("done")

}
