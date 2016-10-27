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
  def esDeviceDataIndex: String = config.getString(ConfigKeys.DEVICE_DATA_DB_INDEX)

  /**
    * @return Elasticsearch DeviceData type
    */
  def esDeviceDataType: String = config.getString(ConfigKeys.DEVICE_DATA_DB_TYPE)

  /**
    * @return ElasticSearch default size in regards to pagination
    */
  def esDefaultPageSize: Int = config.getInt(ConfigKeys.ES_DEFAULT_PAGE_SIZE)

}
