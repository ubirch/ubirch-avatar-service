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
    *
    * @return
    */
  def esHost: String = config.getString(ConfigKeys.ESHOST)

  /**
    *
    * @return
    */
  def esPort: Int = config.getInt(ConfigKeys.ESPORT)

  def deviceDataDbHost:String = config.getString(ConfigKeys.DEVICE_DATA_DB_HOST)

  def deviceDataDbPortBinary: Int = config.getInt(ConfigKeys.DEVICE_DATA_DB_PORT_BINARY)

  def deviceDataDbPortHttp: Int = config.getInt(ConfigKeys.DEVICE_DATA_DB_PORT_HTTP)

  def deviceDataDbIndex:String = config.getString(ConfigKeys.DEVICE_DATA_DB_INDEX)

  def deviceDataDbType:String = config.getString(ConfigKeys.DEVICE_DATA_DB_TYPE)

  def deviceDataDbUser:String = config.getString(ConfigKeys.DEVICE_DATA_DB_USER)

  def deviceDataDbPassword:String = config.getString(ConfigKeys.DEVICE_DATA_DB_PASSWORD)

  /**
    * @return ElasticSearch default size in regards to pagination
    */
  def deviceDataDbDefaultPageSize: Int = config.getInt(ConfigKeys.DEVICE_DATA_DB_DEFAULT_PAGE_SIZE)

}
