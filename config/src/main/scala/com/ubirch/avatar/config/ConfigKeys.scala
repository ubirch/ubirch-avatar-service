package com.ubirch.avatar.config

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object ConfigKeys {

  private final val prefix = "ubirchAvatarService"

  final val HTTPINTERFACE = s"$prefix.interface"
  final val HTTPPORT = s"$prefix.port"

  final val ESHOST = s"$prefix.es.host"
  final val ESPORT = s"$prefix.es.port"

}
