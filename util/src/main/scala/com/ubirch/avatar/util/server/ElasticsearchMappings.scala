package com.ubirch.avatar.util.server

import java.net.URL

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.Status._

/**
  * author: cvandrei
  * since: 2017-01-10
  */
trait ElasticsearchMappings extends StrictLogging {

  private val indexInfoDevice = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceIndex)
  private val indexInfoDeviceRawData = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceDataRawIndex)
  private val indexInfoDeviceHistory = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceDataProcessedIndex)
  private val indexInfoDeviceType = IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceTypeIndex)

  final val indexInfos: Seq[IndexInfo] = Seq(
    indexInfoDevice,
    indexInfoDeviceRawData,
    indexInfoDeviceHistory,
    indexInfoDeviceType
  )

  final def createElasticsearchMappings(): Unit = {

    Seq(
      deviceMappings,
      deviceDataRawMappings,
      deviceDataProcessedMappings,
      deviceTypeMappings
    ) foreach create

  }

  private def create(mapping: Mapping) = {

    val httpClient = new HttpClient
    val body = Some(RequestBody(mapping.mappings, APPLICATION_JSON))
    val res = httpClient.post(mapping.url, body)

    res.status match {
      case S200_OK => logger.info(s"Elasticsearch index and mapping created: ${mapping.url}")
      case S400_BadRequest => logger.info(s"Elasticsearch index and mapping already exists: ${mapping.url}")
      case _ => logger.error(s"failed to create Elasticsearch index and mapping: ${mapping.url} (statusCode=${res.status})")
    }

  }

  private val deviceMappings: Mapping = {

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

  private val deviceDataRawMappings: Mapping = {

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

  private val deviceDataProcessedMappings: Mapping = {

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

  private val deviceTypeMappings: Mapping = {

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