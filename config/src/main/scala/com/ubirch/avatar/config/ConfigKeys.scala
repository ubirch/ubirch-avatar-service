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

  final val deviceDataDbPrefix = s"$prefix.deviceData.db"
  final val DEVICE_DATA_DB_HOST = s"$deviceDataDbPrefix.host"
  final val DEVICE_DATA_DB_PORT = s"$deviceDataDbPrefix.port"
  final val DEVICE_DATA_DB_INDEX = s"$deviceDataDbPrefix.index"
  final val DEVICE_DATA_DB_USER = s"$deviceDataDbPrefix.user"
  final val DEVICE_DATA_DB_PASSWORD = s"$deviceDataDbPrefix.password"
  final val DEVICE_DATA_DB_DEFAULT_PAGE_SIZE = s"$deviceDataDbPrefix.defaultPageSize"

}
