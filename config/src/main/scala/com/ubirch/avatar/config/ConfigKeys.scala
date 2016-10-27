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

  // AWS local mode defines whether app is running on a locally or at AWS
  final val AWS_LOCAL_MODE = s"$esPrefix.aws.localmode"

  // AWS base MQTT topic name for all AWS IoT Things
  final val AWS_TOPICS_BASENAME = s"$esPrefix.aws.topics.basename"

  // AWS IoT desired state name
  final val AWS_STATES_DESIRED = s"$esPrefix.aws.states.desired"

  // AWS IoT reported state name
  final val AWS_STATES_REPORTED = s"$esPrefix.aws.states.reported"
}
