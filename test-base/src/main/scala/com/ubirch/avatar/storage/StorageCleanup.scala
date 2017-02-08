package com.ubirch.avatar.storage

import com.ubirch.avatar.util.server.ElasticsearchMappings

import uk.co.bigbeeconsultants.http.HttpClient

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait StorageCleanup extends ElasticsearchMappings {

  /**
    * Clean Elasticsearch instance by running the following operations:
    *
    * * delete indexes
    * * create mappings
    */
  final def cleanElasticsearch(): Unit = {

    deleteIndexes()
    Thread.sleep(200)

    createElasticsearchMappings()
    Thread.sleep(100)

  }

  /**
    * Delete all indexes.
    */
  final def deleteIndexes(): Unit = {

    val httpClient = new HttpClient
    indexInfos foreach { indexTuple =>
      httpClient.delete(indexTuple.url)
    }

  }

}
