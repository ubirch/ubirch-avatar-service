package com.ubirch.avatar.storage

import com.ubirch.avatar.util.server.ElasticsearchMappings
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.client.Requests
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.index.IndexNotFoundException

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait ESStorageCleanup extends ElasticsearchMappings {

  implicit protected val esClient: TransportClient = ESSimpleStorage.getCurrentEsClient

  final def esClientClose(): Unit = esClient.close()

  /**
    * Clean Elasticsearch instance by running the following operations:
    *
    * * delete indexes
    * * create mappings
    */
  final def cleanElasticsearch()(implicit esClient: TransportClient): Unit = {

    deleteIndices()
    Thread.sleep(200)

    createElasticsearchMappings()
    Thread.sleep(100)

  }

  /**
    * Delete all indexes.
    */
  final def deleteIndices()(implicit esClient: TransportClient): Unit = {

    for (index <- indicesToDelete) {

      try {

        val deleteRequest = Requests.deleteIndexRequest(index)
        val response: DeleteIndexResponse = esClient.admin().indices().delete(deleteRequest).actionGet()

        response.isAcknowledged match {
          case true => logger.info(s"deleted index: '$index'")
          case false => logger.error(s"failed to delete  index: '$index'")
        }

      } catch {
        case _: IndexNotFoundException => logger.info(s"unable to delete non-existing index: $index")
      }

    }

  }

}
