package com.ubirch.server.util

import java.util.Base64

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.crypto.codec.CodecUtil
import com.ubirch.crypto.ecc.EccUtil

object ServerKeys extends StrictLogging {

  private final val KEYLEN: Int = 64

  //private final val pubKeyHex: String = Config.serverPublicKey
  private final val pubKeyHex: String = Config.serverPrivateKey.takeRight(KEYLEN)
  logger.debug(s"pubKeyHex: $pubKeyHex")
  private final val privKeyHex: String = Config.serverPrivateKey.take(KEYLEN)

  private final val pubKeyBin: Array[Byte] = CodecUtil.multiDecoder(pubKeyHex).get
  private final val privKeyBin: Array[Byte] = CodecUtil.multiDecoder(privKeyHex).get

  final val publicKey = EccUtil.decodePublicKey(pubKeyBin)

  final val privateKey = EccUtil.decodePrivateKey(privKeyBin)

  final val pubKeyEnc: String = Base64.getEncoder.encodeToString(pubKeyBin)

  final val privKeyEnc: String = Base64.getEncoder.encodeToString(privKeyBin)

}
