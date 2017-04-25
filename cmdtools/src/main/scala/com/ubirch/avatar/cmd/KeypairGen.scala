package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.keyservice.KeyServiceManager

/**
  * Created by derMicha on 14/11/16.
  */
object KeypairGen extends App with StrictLogging {

  val keyPair = KeyServiceManager.createKeyPair
  val sKey = keyPair.getPrivate
  val pKey = keyPair.getPublic

  logger.debug(s"skey: ${sKey.getFormat}")
  logger.debug(s"skey: ${KeyServiceManager.encodePrivateKey(sKey)}")
  logger.debug(s"pkey: ${pKey.getFormat}")
  logger.debug(s"pkey: ${KeyServiceManager.encodePubKey(pKey)}")
}
