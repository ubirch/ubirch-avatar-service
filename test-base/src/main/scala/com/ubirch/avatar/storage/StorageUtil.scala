package com.ubirch.avatar.storage

import java.net.URL

import com.ubirch.avatar.config.Config
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType.APPLICATION_JSON
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.response.Status._

/**
  * Created by derMicha on 02/01/17.
  */
object StorageUtil {

  private val indexInfoDevice = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceIndex)
  private val indexInfoDeviceRawData = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceDataRawIndex)
  private val indexInfoDeviceHistory = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceDataProcessedIndex)
  private val indexInfoDeviceType = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceTypeIndex)

  private val indexInfos: Seq[IndexInfo] = Seq(
    indexInfoDevice,
    indexInfoDeviceRawData,
    indexInfoDeviceHistory,
    indexInfoDeviceType
  )

  private val mappings = Seq(
    deviceMappings,
    deviceDataRawMappings,
    deviceDataProcessedMappings,
    deviceTypeMappings
  )

  /**
    * Delete all indexes.
    */
  final def deleteIndexes(): Unit = {

    val httpClient = new HttpClient
    indexInfos foreach { indexTuple =>
      httpClient.delete(indexTuple.url)
    }
    Thread.sleep(200)
  }

  /**
    * Create all mappings.
    */
  final def createMappings(): Unit = {

    mappings foreach storeMapping
    Thread.sleep(100)

  }

  final def createMissing(): Unit = {

    val httpClient = new HttpClient


  }

  private def storeMapping(mapping: Mapping) = {

    val httpClient = new HttpClient
    val body = Some(RequestBody(mapping.mappings, APPLICATION_JSON))
    val res = httpClient.post(mapping.url, body)

    if (res.status != S200_OK) {
      throw new IllegalArgumentException(s"creating Elasticsearch mappings failed: ${res.body.asString}")
    }

  }

  private def deviceMappings: Mapping = {

    val deviceMapping =
      s"""{
         |  "mappings": {
         |    "${Config.esDeviceType}" : {
         |      "properties" : {
         |        "deviceId" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "hwDeviceId" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "hashedHwDeviceId" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        }
         |      }
         |    }
         |  }
         |}""".stripMargin
    val url = indexInfoDevice.url

    Mapping(url, deviceMapping)

  }

  private def deviceDataRawMappings: Mapping = {

    val deviceDataRawMapping =
      s"""{
         |  "mappings": {
         |    "${Config.esDeviceDataRawType}" : {
         |      "properties" : {
         |        "a" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "id" : {
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
         |    "${Config.esDeviceDataProcessedType}" : {
         |      "properties" : {
         |        "deviceId" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "messageId" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "deviceDataRawId" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "id" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "a" : {
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

  private def deviceTypeMappings: Mapping = {

    val deviceTypeMapping =
      s"""{
         |  "mappings": {
         |    "${Config.esDeviceTypeType}" : {
         |      "properties" : {
         |        "key" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        }
         |      }
         |    }
         |  }
         |}""".stripMargin
    val url = indexInfoDeviceType.url

    Mapping(url, deviceTypeMapping)

  }

}

case class IndexInfo(host: String, port: Int, index: String) {
  def url: URL = new URL(s"http://$host:$port/$index")
}

case class Mapping(url: URL, mappings: String)
