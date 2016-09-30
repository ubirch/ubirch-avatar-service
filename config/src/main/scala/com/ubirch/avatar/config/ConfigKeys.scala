package com.ubirch.avatar.config

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object ConfigKeys {

  private final val prefix = "ubirchAvatarService"

  final val INTERFACE = s"$prefix.interface"
  final val PORT = s"$prefix.port"

  final val esPrefix = s"$prefix.deviceData.elastic"
  final val ES_PROTOCOL = s"$esPrefix.protocol"
  final val ES_HOST = s"$esPrefix.host"
  final val ES_PORT = s"$esPrefix.port"
  final val ES_INDEX = s"$esPrefix.index"
  final val ES_USER = s"$esPrefix.user"
  final val ES_PASSWORD = s"$esPrefix.password"
  final val ES_DEFAULT_SIZE = s"$esPrefix.defaultSize"

}
