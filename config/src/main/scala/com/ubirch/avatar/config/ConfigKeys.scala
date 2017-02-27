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

  /*
   * Akka related configs
   *********************************************************************************************/

  private val akkaPrefix = s"$prefix.akka"

  final val ACTOR_TIMEOUT = s"$akkaPrefix.actorTimeout"
  final val AKKA_NUMBER_OF_WORKERS = s"$akkaPrefix.numberOfWorkers"

  /* Elasticsearch Related Config Keys
   **********************************************************************/

  // Prefixes
  final val esPrefix = s"$prefix.es"
  final val esPortPrefix = s"$esPrefix.port"
  final val esDevicePrefix = s"$esPrefix.device"
  final val esDeviceRawDataPrefix = s"$esPrefix.devicerawdata"
  final val esDeviceRawDataAnchoredPrefix = s"$esPrefix.devicerawdataAnchored"
  final val esDeviceHistoryPrefix = s"$esPrefix.devicehistory"
  final val esDeviceTypePrefix = s"$esPrefix.devicetype"
  final val esAvatarStatePrefix = s"$esPrefix.avatarState"

  // Connection
  final val ESPORT_REST = s"$esPortPrefix.rest"
  final val DEVICE_DATA_DB_USER = s"$esPrefix.user"
  final val DEVICE_DATA_DB_PASSWORD = s"$esPrefix.password"

  // Device Index & Type
  final val ES_DEVICE_INDEX = s"$esDevicePrefix.index"
  final val ES_DEVICE_TYPE = s"$esDevicePrefix.type"

  // DeviceRawData Index & Type
  final val ES_DEVICE_DATA_RAW_INDEX = s"$esDeviceRawDataPrefix.index"
  final val ES_DEVICE_DATA_RAW_TYPE = s"$esDeviceRawDataPrefix.type"

  // anchored DeviceRawData (with txHash) Index & Type
  final val ES_DEVICE_DATA_RAW_ANCHORED_INDEX = s"$esDeviceRawDataAnchoredPrefix.index"
  final val ES_DEVICE_DATA_RAW_ANCHORED_TYPE = s"$esDeviceRawDataAnchoredPrefix.type"

  // DeviceHistory Index & Type
  final val ES_DEVICE_DATA_PROCESSED_INDEX = s"$esDeviceHistoryPrefix.index"
  final val ES_DEVICE_DATA_PROCESSED_TYPE = s"$esDeviceHistoryPrefix.type"

  // Device Type
  final val ES_DEVICE_TYPE_INDEX = s"$esDeviceTypePrefix.index"
  final val ES_DEVICE_TYPE_TYPE = s"$esDeviceTypePrefix.type"

  // Avatar State
  final val ES_AVATAR_STATE_INDEX = s"$esAvatarStatePrefix.index"
  final val ES_AVATAR_STATE_TYPE = s"$esAvatarStatePrefix.type"

  // Misc
  final val ES_DEFAULT_PAGE_SIZE = s"$esPrefix.defaultPageSize"
  final val ES_LARGE_PAGE_SIZE = s"$esPrefix.largePageSize"

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
  final val AWS_ACCESS_KEY = s"$awsPrefix.awsaccesskey"

  // AWS Auth keys
  final val AWS_SECRET_ACCESS_KEY = s"$awsPrefix.awssecretaccesskey"

  // AWS IoT
  final val AWS_IOT_ENV_PREFIX = s"$awsPrefix.iot.env.prefix"

  // AWS SQS queues
  final val AWS_SQS_QUEUES_TRANSFORMER = s"$awsPrefix.sqs.queues.transformer"
  final val AWS_SQS_QUEUES_TRANSFORMER_OUT = s"$awsPrefix.sqs.queues.transformer_out"

  /* REST Client Related Config Keys
   **********************************************************************/

  final val restClientPrefix = s"$prefix.restclient"
  final val restClientTimeout = s"$restClientPrefix.timeout"

  final val REST_CLIENT_TIMEOUT_CONNECT = s"$restClientTimeout.connect"
  final val REST_CLIENT_TIMEOUT_READ = s"$restClientTimeout.read"

  /* MQTT Related Config Keys
 **********************************************************************/

  final val mqttPrefix = s"$prefix.mqtt"

  final val MQTT_BROKER_URL = s"$mqttPrefix.broker.url"

  final val MQTT_USER_KEY = s"$mqttPrefix.credentials.user"

  final val MQTT_PASSWORD_KEY = s"$mqttPrefix.credentials.password"

  final val MQTT_QUEUES_DEVICES_BASE = s"$mqttPrefix.queues.deviceBaseTopic"

  final val MQTT_QUEUES_DEVICES_IN = s"$mqttPrefix.queues.devicesTopicPartIn"

  final val MQTT_QUEUES_DEVICES_OUT = s"$mqttPrefix.queues.devicesTopicPartOut"

}
