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

  private val indexInfoDevice = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceIndex)
  private val indexInfoDeviceRawData = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceRawDataIndex)
  private val indexInfoDeviceHistory = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceHistoryIndex)
  private val indexInfos: Seq[IndexInfo] = Seq(
    indexInfoDevice,
    indexInfoDeviceRawData,
    indexInfoDeviceHistory
  )

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

    val mappings = Seq(
      deviceDataRawMappings,
      deviceDataProcessedMappings
    )
    mappings foreach storeMapping

  }

  private def storeMapping(mapping: Mapping) = {

    val httpClient = new HttpClient
    val body = Some(RequestBody(mapping.mappings, APPLICATION_JSON))
    val res = httpClient.post(mapping.url, body)

    if (res.status != S200_OK) {
      throw new IllegalArgumentException(s"creating Elasticsearch mappings failed: ${res.body.asString}")
    }

  }

  private def deviceDataRawMappings: Mapping = {

    val deviceDataRawMapping =
      s"""{
          |  "mappings": {
          |    "${Config.esDeviceRawDataType}" : {
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
    val url = indexInfoDeviceRawData.url

    Mapping(url, deviceDataRawMapping)

  }

  private def deviceDataProcessedMappings: Mapping = {

    val deviceDataProcessedMapping =
      s"""{
          |  "mappings": {
          |    "${Config.esDeviceHistoryType}" : {
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
    val url = indexInfoDeviceHistory.url

    Mapping(url, deviceDataProcessedMapping)

  }

}

case class IndexInfo(host: String, port: Int, index: String) {
  def url: URL = new URL(s"http://$host:$port/$index")
}

case class Mapping(url: URL, mappings: String)