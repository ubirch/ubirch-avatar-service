package com.ubirch.services.util

import java.security.MessageDigest
import java.util.Base64

import com.ubirch.avatar.config.Const

/**
  * Created by derMicha on 20/04/16.
  */
object SimpleHashUtil {

  def hashStringDefault(data: String): String = {
    hashString256B64(data = data)
  }

  def hashString256Hex(data: String): String = {
    hashString(data = data, algorithm = Const.HASHSHA256, encoding = Const.ENCODING_HEX)
  }

  def hashString256B64(data: String): String = {
    hashString(data = data, algorithm = Const.HASHSHA256, encoding = Const.ENCODING_B64)
  }

  def hashString512Hex(data: String): String = {
    hashString(data = data, algorithm = Const.HASHSHA512, encoding = Const.ENCODING_HEX)
  }

  def hashString512B64(data: String): String = {
    hashString(data = data, algorithm = Const.HASHSHA512, encoding = Const.ENCODING_B64)
  }

  private def hashString(data: String, algorithm: String, encoding: String): String = {

    val digest = MessageDigest.getInstance(algorithm).digest(data.getBytes("UTF-8"))

    if (Const.ENCODING_HEX.equals(encoding))
      bytes2hex(digest)
    else
      new String(Base64.getEncoder.encode(digest))
  }

  private def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  private def bytes2hex(bytes: Array[Byte], sep: Option[String] = None): String = {
    sep match {
      case None => bytes.map("%02x".format(_)).mkString
      case _ => bytes.map("%02x".format(_)).mkString(sep.get)
    }
  }
}
