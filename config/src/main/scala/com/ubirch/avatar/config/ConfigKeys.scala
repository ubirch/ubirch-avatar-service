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
  final val TRACKLE_AUTH_TOKEN = s"$prefix.ubirch.trackleAuthToken"

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
  final val esDeviceRawDataAnchoredPrefix = s"$esPrefix.devicerawdataAnchored"
  final val esDeviceHistoryPrefix = s"$esPrefix.devicehistory"
  final val esDeviceTypePrefix = s"$esPrefix.devicetype"
  final val esDeviceStatePrefix = s"$esPrefix.devicestate"

  // Device Index & Type
  final val ES_DEVICE_INDEX = s"$esDevicePrefix.index"
  final val ES_DEVICE_TYPE = s"$esDevicePrefix.type"

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

  // server ecc signing private key
  final val SIGNING_PRIVATE_KEY = "crypto.ecc.signingPrivateKey"

  //KAFKA related configs
  final val kafkaPrefix = s"$prefix.kafka"
  final val kafkaConPrefix: String = s"$kafkaPrefix.consumer"

  final val KAFKA_PROD_BOOTSTRAP_SERVER = s"$kafkaPrefix.producer.bootstrapServers"
  final val KAFKA_CONS_BOOTSTRAP_SERVER = s"$kafkaConPrefix.bootstrapServers"

  // consumer related keys
  final val KAFKA_RETRY_MIN_BACKOFF: String = s"$kafkaConPrefix.retryMinBackoff"
  final val KAFKA_RETRY_MAX_BACKOFF: String = s"$kafkaConPrefix.retryMaxBackoff"
  final val KAFKA_RETRY_BACKOFF_FACTOR: String = s"$kafkaConPrefix.retryBackoffFactor"
  final val KAFKA_RETRY_MAX_RETRIES: String = s"$kafkaConPrefix.retryMaxRetries"
  final val KAFKA_SUBSCRIBE_PARALLEL: String = s"$kafkaConPrefix.parallel"
  final val KAFKA_MAX_COMMIT: String = s"$kafkaConPrefix.maxCommit"
  final val KAFKA_TRACKLE_END_OF_LIFE_GROUP = s"$kafkaConPrefix.trackleEndOfLifeGroup"

  // topics
  final val KAFKA_TRACKLE_MSGPACK_TOPIC = s"$kafkaPrefix.trackleMsgpackTopic"
  final val KAFKA_TRACKLE_END_OF_LIFE_TOPIC = s"$kafkaPrefix.trackleEndOfLifeTopic"
}
