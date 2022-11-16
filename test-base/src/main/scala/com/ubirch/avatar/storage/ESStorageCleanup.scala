package com.ubirch.avatar.storage

import com.ubirch.avatar.util.server.ElasticsearchMappings
import com.ubirch.util.elasticsearch.EsHighLevelClient.esClient

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait ESStorageCleanup extends ElasticsearchMappings {

  final def esClientClose(): Unit = esClient.close()

}
