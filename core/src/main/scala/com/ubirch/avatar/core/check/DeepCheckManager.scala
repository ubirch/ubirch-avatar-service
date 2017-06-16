package com.ubirch.avatar.core.check

import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-08
  */
object DeepCheckManager {

  /**
    * Check if we can run a simple query on the database.
    *
    * @return deep check response with _status:OK_ if ok; otherwise with _status:NOK_
    */
  //@TODO MQTT / SQS connections have to be checked
  def connectivityCheck(): Future[DeepCheckResponse] = ESSimpleStorage.connectivityCheck()

}
