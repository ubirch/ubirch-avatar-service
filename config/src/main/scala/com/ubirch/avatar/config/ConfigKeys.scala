package com.ubirch.avatar.config

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object ConfigKeys {

  private final val prefix = "ubirchAvatarService"

  final val HTTPINTERFACE = s"$prefix.interface"
  final val HTTPPORT = s"$prefix.port"

  /* Elasticsearch Config Keys
   **********************************************************************/

  // Prefixes
  final val esPrefix = s"$prefix.es"
  final val esPortPrefix = s"$esPrefix.port"
  final val deviceDataPrefix = s"$esPrefix.deviceData"

  // Connection
  final val ESHOST = s"$esPrefix.host"
  final val ESPORT_BINARY = s"$esPortPrefix.binary"
  final val ESPORT_REST = s"$esPortPrefix.rest"
  final val DEVICE_DATA_DB_USER = s"$esPrefix.user"
  final val DEVICE_DATA_DB_PASSWORD = s"$esPrefix.password"

  // DeviceData Index & Type
  final val DEVICE_DATA_DB_INDEX = s"$deviceDataPrefix.index"
  final val DEVICE_DATA_DB_TYPE = s"$deviceDataPrefix.type"

  // Misc
  final val ES_DEFAULT_PAGE_SIZE = s"$esPrefix.defaultPageSize"

}
