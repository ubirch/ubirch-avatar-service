package com.ubirch.avatar.cmd

import java.io.{File, FileOutputStream}
import java.util.Base64

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.crypto.ecc.EccUtil
import net.i2p.crypto.eddsa.{EdDSAPrivateKey, EdDSAPublicKey}
import org.apache.commons.codec.binary.Hex
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
  * Created by derMicha on 14/11/16.
  */
object KeypairGen extends App with StrictLogging {

  val envs = List(
    "master",
    "prod",
    "demo",
    "local"
  )

  val basePath = "/Volumes/ubirch master keys/ubirch"

  val prkBaseFilename = "-backend-ed25519.prk"
  val pbkBaseFilename = "-backend-ed25519.pbk"

  val now = DateTime.now
  val fmt = DateTimeFormat.forPattern("yyyyMMdd")
  val nowStr = fmt.print(now)

  envs.foreach { env =>
    val (pubKey, prvKey) = EccUtil.generateEccKeyPair

    val pubKeyBin = EdDSAPublicKey.decode(pubKey.getEncoded)
    val prvKeyBin = EdDSAPrivateKey.decode(prvKey.getEncoded)

    writeBinData(s"${basePath}/${nowStr}_${env}${prkBaseFilename}.bin", prvKeyBin)
    writeBinData(s"${basePath}/${nowStr}_${env}${pbkBaseFilename}.bin", pubKeyBin)

    val pubKeyHex = Hex.encodeHexString(pubKeyBin)
    val prvKeyHex = Hex.encodeHexString(prvKeyBin)

    writeBinData(s"${basePath}/${nowStr}_${env}${prkBaseFilename}.hex", prvKeyHex.getBytes)
    writeBinData(s"${basePath}/${nowStr}_${env}${pbkBaseFilename}.hex", pubKeyHex.getBytes)

    val pubKey64 = java.util.Base64.getEncoder.encodeToString(pubKeyBin)
    val prvKey64 = java.util.Base64.getEncoder.encodeToString(prvKeyBin)

    writeBinData(s"${basePath}/${nowStr}_${env}${prkBaseFilename}.b64", prvKey64.getBytes)
    writeBinData(s"${basePath}/${nowStr}_${env}${pbkBaseFilename}.b64", pubKey64.getBytes)
  }


  def writeBinData(filename: String, binData: Array[Byte]): Unit = {
    val fos: FileOutputStream = new FileOutputStream(new File(filename))
    fos.write(binData, 0, binData.length)
    fos.flush()
    fos.close()
  }

}
