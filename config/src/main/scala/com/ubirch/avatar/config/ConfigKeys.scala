package com.ubirch.avatar.config

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object ConfigKeys {

  private final val prefix = "ubirchAvatarService"

  final val HTTPINTERFACE = s"$prefix.interface"
  final val HTTPPORT = s"$prefix.port"

  /* Elasticsearch Related Config Keys
   **********************************************************************/

  // Prefixes
  final val esPrefix = s"$prefix.es"
  final val esPortPrefix = s"$esPrefix.port"
  final val esDevicePrefix = s"$esPrefix.device"
  final val esDeviceRawDataPrefix = s"$esPrefix.devicerawdata"
  final val esDeviceHistoryPrefix = s"$esPrefix.devicehistory"

  final val awsPrefix = s"$prefix.aws"
  // Connection
  final val ESHOST = s"$esPrefix.host"
  final val ESPORT_BINARY = s"$esPortPrefix.binary"
  final val ESPORT_REST = s"$esPortPrefix.rest"
  final val DEVICE_DATA_DB_USER = s"$esPrefix.user"
  final val DEVICE_DATA_DB_PASSWORD = s"$esPrefix.password"

  // Device Index & Type
  final val ES_DEVICE_INDEX = s"$esDevicePrefix.index"
  final val ES_DEVICE_TYPE = s"$esDevicePrefix.type"
  // DeviceRawData Index & Type
  final val ES_DEVICE_RAW_DATA_INDEX = s"$esDeviceRawDataPrefix.index"
  final val ES_DEVICE_RAW_DATA_TYPE = s"$esDeviceRawDataPrefix.type"

  // DeviceHistory Index & Type
  final val ES_DEVICE_HISTORY_INDEX = s"$esDeviceHistoryPrefix.index"
  final val ES_DEVICE_HISTORY_TYPE = s"$esDeviceHistoryPrefix.type"

  // Misc
  final val ES_DEFAULT_PAGE_SIZE = s"$esPrefix.defaultPageSize"

  /* AWS Related Config Keys
   **********************************************************************/

  // AWS local mode defines whether app is running on a locally or at AWS
  final val AWS_LOCAL_MODE = s"$awsPrefix.localmode"

  // AWS base MQTT topic name for all AWS IoT Things
  final val AWS_TOPICS_BASENAME = s"$awsPrefix.topics.basename"

  // AWS IoT desired state name
  final val AWS_STATES_DESIRED = s"$awsPrefix.states.desired"

  // AWS IoT reported state name
  final val AWS_STATES_REPORTED = s"$awsPrefix.states.reported"
}
