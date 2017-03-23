package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.util.model.DeviceUtil

/**
  * Created by derMicha on 14/11/16.
  */
object KeypairGen extends App with StrictLogging {

  val (sKey, pKey) = DeviceUtil.createKeyPair


  logger.debug(s"skey: ${sKey.getFormat}")
  logger.debug(s"skey: ${bytes2hex(sKey.getEncoded)}")
  logger.debug(s"pkey: ${pKey.getFormat}")
  logger.debug(s"pkey: ${bytes2hex(pKey.getEncoded)}")


  private def bytes2hex(bytes: Array[Byte], sep: Option[String] = None): String = {
    sep match {
      case None => bytes.map("%02x".format(_)).mkString
      case _ => bytes.map("%02x".format(_)).mkString(sep.get)
    }

  }
}
