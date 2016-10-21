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

  /**
    * @return ElasticSearch default size in regards to pagination
    */
  def esDefaultSize: Int = config.getInt(ConfigKeys.ES_DEFAULT_SIZE)

}
