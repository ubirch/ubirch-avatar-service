package com.ubirch.avatar.config

import com.ubirch.util.config.ConfigBase

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object Config extends ConfigBase {

  def goPipelineName: String = config.getString(ConfigKeys.GO_PIPELINE_NAME)

  def goPipelineLabel: String = config.getString(ConfigKeys.GO_PIPELINE_LABEL)

  def goPipelineRevision: String = config.getString(ConfigKeys.GO_REVISION_GIT)

  def getMessageMaxAge: Long = config.getLong(ConfigKeys.MESSAGEMAXAGE)

  def getMessageSignatureCache: Long = config.getLong(ConfigKeys.MESSAGESIGNATURECACHE)

  /*
   * Service
   ****************************************************************/

  /**
    * @return http protocol
    */
  def protocol: String = config.getString(ConfigKeys.HTTPPROTOCOL)

  /**
    * The interface the server runs on.
    *
    * @return interface
    */
  def httpInterface: String = config.getString(ConfigKeys.HTTPINTERFACE)

  /**
    * The interface the prometheus server runs on.
    *
    * @return pinterface
    */
  def httpPrometheusInterface: String = config.getString(ConfigKeys.HTTPPINTERFACE)

  /**
    * Port the server listens on.
    *
    * @return port number
    */
  def httpPort: Int = config.getInt(ConfigKeys.HTTPPORT)

  /**
    * Port the prometheus server listens on.
    *
    * @return pport number
    */
  def httpPrometheusPort: Int = config.getInt(ConfigKeys.HTTPPPORT)

  def prometheusEnabled: Boolean = config.getBoolean(ConfigKeys.PENABLED)


  /**
    * The interface the server runs on.
    *
    * @return interface
    */
  def udpInterface: String = config.getString(ConfigKeys.UDPINTERFACE)

  /**
    * Port the server listens on.
    *
    * @return port number
    */
  def udpPort: Int = config.getInt(ConfigKeys.UDPPORT)

  def enviroment: String = config.getString(ConfigKeys.ENVIROMENT)

  /*
   * Akka
   ************************************************************************************************/

  /**
    * Default actor timeout.
    *
    * @return timeout in seconds
    */
  def actorTimeout: Int = config.getInt(ConfigKeys.ACTOR_TIMEOUT)

  def akkaNumberOfFrontendWorkers: Int = config.getInt(ConfigKeys.AKKA_NUMBER_OF_FRONTEND_WORKERS)

  def akkaNumberOfBackendWorkers: Int = config.getInt(ConfigKeys.AKKA_NUMBER_OF_BACKEND_WORKERS)

  /*
   * Elasticsearch
   ************************************************************************************************/

  /**
    * @return Elasticsearch DeviceData index
    */
  def esDeviceIndex: String = config.getString(ConfigKeys.ES_DEVICE_INDEX)

  /**
    * @return Elasticsearch DeviceData type
    */
  def esDeviceType: String = config.getString(ConfigKeys.ES_DEVICE_TYPE)

  /**
    * @return Elasticsearch raw device data index
    */
  def esDeviceDataRawIndex: String = config.getString(ConfigKeys.ES_DEVICE_DATA_RAW_INDEX)

  /**
    * @return Elasticsearch raw device data type
    */
  def esDeviceDataRawType: String = config.getString(ConfigKeys.ES_DEVICE_DATA_RAW_TYPE)

  /**
    * @return Elasticsearch anchored raw device data (with txHash) index
    */
  def esDeviceDataRawAnchoredIndex: String = config.getString(ConfigKeys.ES_DEVICE_DATA_RAW_ANCHORED_INDEX)

  /**
    * @return Elasticsearch anchored raw device data (with txHash) type
    */
  def esDeviceDataRawAnchoredType: String = config.getString(ConfigKeys.ES_DEVICE_DATA_RAW_ANCHORED_TYPE)

  /**
    * @return Elasticsearch device history index
    */
  def esDeviceDataHistoryIndex: String = config.getString(ConfigKeys.ES_DEVICE_HISTORY_INDEX)

  /**
    * @return Elasticsearch device history data type
    */
  def esDeviceDataHistoryType: String = config.getString(ConfigKeys.ES_DEVICE_HISTORY_TYPE)

  /**
    * @return Elasticsearch deviceType index
    */
  def esDeviceTypeIndex: String = config.getString(ConfigKeys.ES_DEVICE_TYPE_INDEX)

  /**
    * @return Elasticsearch deviceType type
    */
  def esDeviceTypeType: String = config.getString(ConfigKeys.ES_DEVICE_TYPE_TYPE)

  /**
    * @return Elasticsearch deviceType index
    */
  def esDeviceStateIndex: String = config.getString(ConfigKeys.ES_DEVICE_STATE_INDEX)

  /**
    * @return Elasticsearch deviceType type
    */
  def esDeviceStateType: String = config.getString(ConfigKeys.ES_DEVICE_STATE_TYPE)

  /**
    * @return ElasticSearch default size in regards to pagination
    */
  def esDefaultPageSize: Int = config.getInt(ConfigKeys.ES_DEFAULT_PAGE_SIZE)

  /**
   * @return ElasticSearch size of large pages in regards to pagination
   */
  def esLargePageSize: Int = config.getInt(ConfigKeys.ES_LARGE_PAGE_SIZE)

  /*
   * Mongo Related
   ************************************************************************************************/

  def mongoCollectionAvatarState: String = config.getString(ConfigKeys.COLLECTION_AVATAR_STATE)

  /*
   * MQTT
   ************************************************************************************************/

  def mqttBrokerUrl: String = config.getString(ConfigKeys.MQTT_BROKER_URL)

  def mqttUser: String = config.getString(ConfigKeys.MQTT_USER_KEY)

  def mqttPassword: String = config.getString(ConfigKeys.MQTT_PASSWORD_KEY)

  def mqttTopicDevicesBase: String = config.getString(ConfigKeys.MQTT_QUEUES_DEVICES_BASE)

  def mqttTopicDevicesIn: String = config.getString(ConfigKeys.MQTT_QUEUES_DEVICES_IN)

  def mqttTopicDevicesOut: String = config.getString(ConfigKeys.MQTT_QUEUES_DEVICES_OUT)

  def mqttTopicDevicesProcessed: String = config.getString(ConfigKeys.MQTT_QUEUES_DEVICES_PROCESSED)

  def mqttPublishProcessed: Boolean = config.getBoolean(ConfigKeys.MQTT_PUBLISH_PROCESSED)

  /*
  * Server ECC signing private keys
   */
  def serverPrivateKey: String = config.getString(ConfigKeys.SIGNING_PRIVATE_KEY)


  /*
  * Kafka
   */
  def kafkaBoostrapServer: String = config.getString(ConfigKeys.KAFKA_PROD_BOOTSTRAP_SERVER)
  def kafkaTrackelMsgpackTopic: String = config.getString(ConfigKeys.KAFKA_TRACKLE_MSGPACK_TOPIC)

  def userToken: Option[String] = {

    val key = s"ubirchAvatarService.client.rest.userToken"


    if (config.hasPath(key)) {
      Some(config.getString(key))
    } else {
      None
    }

  }

}
