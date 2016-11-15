package com.ubirch.avatar.config

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object ConfigKeys {

  private final val prefix = "ubirchAvatarService"

  final val HTTPPROTOCOL = s"$prefix.protocol"
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
  final val esDeviceTypePrefix = s"$esPrefix.devicetype"

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
  final val ES_DEVICE_DATA_RAW_INDEX = s"$esDeviceRawDataPrefix.index"
  final val ES_DEVICE_DATA_RAW_TYPE = s"$esDeviceRawDataPrefix.type"

  // DeviceHistory Index & Type
  final val ES_DEVICE_DATA_PROCESSED_INDEX = s"$esDeviceHistoryPrefix.index"
  final val ES_DEVICE_DATA_PROCESSED_TYPE = s"$esDeviceHistoryPrefix.type"

  // Device Type
  final val ES_DEVICE_TYPE_INDEX = s"$esDeviceTypePrefix.index"
  final val ES_DEVICE_TYPE_TYPE = s"$esDeviceTypePrefix.type"

  // Misc
  final val ES_DEFAULT_PAGE_SIZE = s"$esPrefix.defaultPageSize"

  /* AWS Related Config Keys
   **********************************************************************/

  final val awsPrefix = s"$prefix.aws"

  // AWS local mode defines whether app is running on a locally or at AWS
  final val AWS_LOCAL_MODE = s"$awsPrefix.localmode"

  // AWS base MQTT topic name for all AWS IoT Things
  final val AWS_TOPICS_BASENAME = s"$awsPrefix.topics.basename"

  // AWS IoT desired state name
  final val AWS_STATES_DESIRED = s"$awsPrefix.states.desired"

  // AWS IoT reported state name
  final val AWS_STATES_REPORTED = s"$awsPrefix.states.reported"

  // AWS IoT reported state name
  final val AWS_STATES_DELTA = s"$awsPrefix.states.delta"

  // AWS IoT reported state name
  final val AWS_STATES_TIMESTAMP = s"$awsPrefix.states.timestamp"

  // AWS Auth keys
  final val AWS_ACCESS_KEY_ID = s"$awsPrefix.awsaccesskeyid"

  // AWS Auth keys
  final val AWS_SECRET_ACCESS_KEY = s"$awsPrefix.awssecretaccesskey"

  // AWS SQS queues
  final val AWS_SQS_QUEUES_TRANSFORMER = s"$awsPrefix.sqs.queues.transformer"

  /* REST Client Related Config Keys
   **********************************************************************/

  final val restClientPrefix = s"$prefix.restclient"
  final val restClientTimeout = s"$restClientPrefix.timeout"

  final val REST_CLIENT_TIMEOUT_CONNECT = s"$restClientTimeout.connect"
  final val REST_CLIENT_TIMEOUT_READ = s"$restClientTimeout.read"

}
