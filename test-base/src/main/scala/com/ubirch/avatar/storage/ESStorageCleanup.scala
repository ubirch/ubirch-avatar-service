package com.ubirch.avatar.storage

import com.ubirch.avatar.util.server.ElasticsearchMappings
import com.ubirch.util.elasticsearch.EsSimpleClient
import org.elasticsearch.client.RestHighLevelClient

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait ESStorageCleanup extends ElasticsearchMappings {

  implicit protected val esClient: RestHighLevelClient = EsSimpleClient.getCurrentEsClient

  final def esClientClose(): Unit = esClient.close()

}
