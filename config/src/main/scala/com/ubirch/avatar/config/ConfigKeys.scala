package com.ubirch.avatar.config

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object ConfigKeys {

  final val prefix = "ubirchAvatarService"

  final val GO_PIPELINE_NAME = s"$prefix.gopipelinename"
  final val GO_PIPELINE_LABEL = s"$prefix.gopipelinelabel"
  final val GO_REVISION_GIT = s"$prefix.gopipelinerev"

  final val HTTPPROTOCOL = s"$prefix.protocol"
  final val HTTPINTERFACE = s"$prefix.interface"
  final val HTTPPORT = s"$prefix.port"

  final val HTTPPINTERFACE = s"$prefix.prometheus.interface"
  final val HTTPPPORT = s"$prefix.prometheus.port"
  final val PENABLED = s"$prefix.prometheus.enabled"

  final val UDPINTERFACE = s"$prefix.udp.interface"
  final val UDPPORT = s"$prefix.udp.port"

  final val ENVIROMENT = s"$prefix.enviroment"

  final val MESSAGEMAXAGE = s"$prefix.messages.maxage"
  final val MESSAGESIGNATURECACHE = s"$prefix.messages.signaturecache"
  /*
   * Akka related configs
   *********************************************************************************************/

  private val akkaPrefix = s"$prefix.akka"

  final val ACTOR_TIMEOUT = s"$akkaPrefix.actorTimeout"
  final val AKKA_NUMBER_OF_WORKERS = s"$akkaPrefix.numberOfWorkers"
  final val AKKA_NUMBER_OF_FRONTEND_WORKERS = s"$akkaPrefix.numberOfFrontendWorkers"
  final val AKKA_NUMBER_OF_BACKEND_WORKERS = s"$akkaPrefix.numberOfBackendWorkers"

  /* Elasticsearch Related Config Keys
   **********************************************************************/

  // Prefixes
  final val esPrefix = s"$prefix.es"
  final val esDevicePrefix = s"$esPrefix.device"
  final val esDeviceRawDataPrefix = s"$esPrefix.devicerawdata"
  final val esDeviceRawDataAnchoredPrefix = s"$esPrefix.devicerawdataAnchored"
  final val esDeviceHistoryPrefix = s"$esPrefix.devicehistory"
  final val esDeviceTypePrefix = s"$esPrefix.devicetype"
  final val esDeviceStatePrefix = s"$esPrefix.devicestate"

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
  final val ES_DEVICE_HISTORY_INDEX = s"$esDeviceHistoryPrefix.index"
  final val ES_DEVICE_HISTORY_TYPE = s"$esDeviceHistoryPrefix.type"

  // Device Type
  final val ES_DEVICE_TYPE_INDEX = s"$esDeviceTypePrefix.index"
  final val ES_DEVICE_TYPE_TYPE = s"$esDeviceTypePrefix.type"

  // Device State
  final val ES_DEVICE_STATE_INDEX = s"$esDeviceStatePrefix.index"
  final val ES_DEVICE_STATE_TYPE = s"$esDeviceStatePrefix.type"


  // Misc
  final val ES_DEFAULT_PAGE_SIZE = s"$esPrefix.defaultPageSize"
  final val ES_LARGE_PAGE_SIZE = s"$esPrefix.largePageSize"


  // Global Message Queue Stuff
  final val INTERNOUTBOX = "intern-outbox"
  final val EXTERNOUTBOX = "extern-outbox"
  final val DEVICEOUTBOX = "device-outbox"

  /*
   * Mongo
   *********************************************************************************************/

  final val MONGO_PREFIX = s"$prefix.mongo"

  private final val mongoCollection = s"$MONGO_PREFIX.collection"

  final val COLLECTION_AVATAR_STATE = s"$mongoCollection.avatarState"

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

  //AWS Region
  final val AWS_REGION = s"$awsPrefix.region"

  //AWS Queue Owner
  final val AWS_QUEUE_OWNER_ID = s"$awsPrefix.queueOwnerId"

  // AWS SQS queues
  final val AWS_SQS_MAX_MESSAGES_PER_POLL = s"$awsPrefix.sqs.maxMessagesPerPoll"

  final val AWS_SQS_QUEUES_TRANSFORMER = s"$awsPrefix.sqs.queues.transformer"
  final val AWS_SQS_QUEUES_TRANSFORMER_OUT = s"$awsPrefix.sqs.queues.transformer_out"
  final val AWS_SQS_UBIRCH_CHAIN_DEVICE_MSG_IN = s"$awsPrefix.sqs.queues.deviceDataIn"
  final val AWS_SQS_UBIRCH_CHAIN_DEVICE_HASH_IN = s"$awsPrefix.sqs.queues.deviceDataHashIn"

  /* MQTT Related Config Keys
 **********************************************************************/


  final val mqttPrefix = s"$prefix.mqtt"

  final val MQTT_BROKER_URL = s"$mqttPrefix.broker.url"

  final val MQTT_USER_KEY = s"$mqttPrefix.credentials.user"

  final val MQTT_PASSWORD_KEY = s"$mqttPrefix.credentials.password"

  final val MQTT_QUEUES_DEVICES_BASE = s"$mqttPrefix.queues.deviceBaseTopic"

  final val MQTT_QUEUES_DEVICES_IN = s"$mqttPrefix.queues.devicesTopicPartIn"

  final val MQTT_QUEUES_DEVICES_OUT = s"$mqttPrefix.queues.devicesTopicPartOut"

  final val MQTT_QUEUES_DEVICES_PROCESSED = s"$mqttPrefix.queues.devicesTopicPartProcessed"

  final val MQTT_PUBLISH_PROCESSED = s"$mqttPrefix.publishProcessed"

  // server ecc signing private key
  final val SIGNING_PRIVATE_KEY = "crypto.ecc.signingPrivateKey"
}
