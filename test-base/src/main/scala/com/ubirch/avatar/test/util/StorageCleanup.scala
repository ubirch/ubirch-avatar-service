package com.ubirch.avatar.test.util

import java.net.URL

import com.ubirch.avatar.config.Config
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.Status._

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait StorageCleanup {

  private val indexInfoDeviceMessage = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceIndex)
  private val indexInfos: Seq[IndexInfo] = Seq(indexInfoDeviceMessage)

  /**
    * Clean Elasticsearch instance by running the following operations:
    *
    * * delete indexes
    * * create mappings
    */
  final def cleanElasticsearch(): Unit = {
    deleteIndexes()
    createMappings()
  }

  /**
    * Delete all indexes.
    */
  private def deleteIndexes(): Unit = {

    val httpClient = new HttpClient
    indexInfos foreach { indexTuple =>
      httpClient.delete(indexTuple.url)
    }

  }

  /**
    * Create all mappings.
    */
  private def createMappings() = {

    val httpClient = new HttpClient

    val deviceMessageMapping =
      s"""{
          |  "mappings": {
          |    "${Config.esDeviceType}" : {
          |      "properties" : {
          |        "deviceId" : {
          |          "type" : "string",
          |          "index": "not_analyzed"
          |        },
          |        "messageId" : {
          |          "type" : "string",
          |          "index": "not_analyzed"
          |        }
          |      }
          |    }
          |  }
          |}""".stripMargin
    val url = indexInfoDeviceMessage.url
    val body = Some(RequestBody(deviceMessageMapping, APPLICATION_JSON))
    val res = httpClient.post(url, body)

    if (res.status != S200_OK) {
      throw new IllegalArgumentException(s"creating Elasticsearch mappings failed: ${res.body.asString}")
    }

  }

}

case class IndexInfo(host: String, port: Int, index: String) {
  def url: URL = new URL(s"http://$host:$port/$index")
}
