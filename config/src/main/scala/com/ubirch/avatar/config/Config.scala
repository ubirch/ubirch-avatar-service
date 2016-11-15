package com.ubirch.avatar.config

import com.ubirch.util.config.ConfigBase

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object Config extends ConfigBase {

  /*
   * SERVER RELATED
   ****************************************************************/

  final val sdmV001 = "0.0.1"
  final val sdmV002 = "0.0.2"
  final val sdmV003 = "0.0.3"

  /**
    * The interface the server runs on.
    *
    * @return interface
    */
  def interface: String = config.getString(ConfigKeys.HTTPINTERFACE)

  /**
    * Port the server listens on.
    *
    * @return port number
    */
  def port: Int = config.getInt(ConfigKeys.HTTPPORT)

  /**
    * @return Elasticsearch host
    */
  def esProtocol: String = config.getString(ConfigKeys.ES_PROTOCOL)

  /**
    * @return Elasticsearch host
    */
  def esHost: String = config.getString(ConfigKeys.ESHOST)

  /**
    * @return Elasticsearch binary client port
    */
  def esPortBinary: Int = config.getInt(ConfigKeys.ESPORT_BINARY)

  /**
    * @return Elasticsearch REST client port
    */
  def esPortHttp: Int = config.getInt(ConfigKeys.ESPORT_REST)

  /**
    * @return Elasticsearch login user (not yet implemented)
    */
  def esUser: String = config.getString(ConfigKeys.DEVICE_DATA_DB_USER)

  /**
    * @return Elasticsearch login password (not yet implemented)
    */
  def esPassword: String = config.getString(ConfigKeys.DEVICE_DATA_DB_PASSWORD)

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
    * @return Elasticsearch processed device data index
    */
  def esDeviceDataProcessedIndex: String = config.getString(ConfigKeys.ES_DEVICE_DATA_PROCESSED_INDEX)

  /**
    * @return Elasticsearch processed device data type
    */
  def esDeviceDataProcessedType: String = config.getString(ConfigKeys.ES_DEVICE_DATA_PROCESSED_TYPE)

  /**
    * @return Elasticsearch deviceType index
    */
  def esDeviceTypeIndex: String = config.getString(ConfigKeys.ES_DEVICE_TYPE_INDEX)

  /**
    * @return Elasticsearch deviceType type
    */
  def esDeviceTypeType: String = config.getString(ConfigKeys.ES_DEVICE_TYPE_TYPE)

  /**
    * @return ElasticSearch default size in regards to pagination
    */
  def esDefaultPageSize: Int = config.getInt(ConfigKeys.ES_DEFAULT_PAGE_SIZE)

  /**
    * AWS local mode defines whether app is running on a locally or at AWS
    *
    * @return boolean value
    */
  def awsLocalMode: Boolean = config.getBoolean(ConfigKeys.AWS_LOCAL_MODE)

  /**
    * AWS base MQTT topic name for all AWS IoT Things
    *
    * @return boolean value
    */
  def awsTopicsBasename: String = config.getString(ConfigKeys.AWS_TOPICS_BASENAME)

  /**
    * @return boolean value
    */
  def awsStatesDesired: String = config.getString(ConfigKeys.AWS_STATES_DESIRED)

  /**
    * @return boolean value
    */
  def awsStatesReported: String = config.getString(ConfigKeys.AWS_STATES_REPORTED)

  /**
    * @return boolean value
    */
  def awsStatesDelta: String = config.getString(ConfigKeys.AWS_STATES_DELTA)

  /**
    * @return boolean value
    */
  def awsStatesTimestamp: String = config.getString(ConfigKeys.AWS_STATES_TIMESTAMP)


  def awsAccessKey: String = config.getString(ConfigKeys.AWS_ACCESS_KEY_ID)

  def awsSecretAccessKey: String = config.getString(ConfigKeys.AWS_SECRET_ACCESS_KEY)

  def awsSqsQueueTransformer: String = config.getString(ConfigKeys.AWS_SQS_QUEUES_TRANSFORMER)
}
