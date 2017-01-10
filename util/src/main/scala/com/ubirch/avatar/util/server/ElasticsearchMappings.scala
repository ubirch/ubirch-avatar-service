package com.ubirch.avatar.util.server

import com.ubirch.avatar.config.Config
import com.ubirch.util.elasticsearch.util.{ElasticsearchMappingsBase, IndexInfo, Mapping}

/**
  * author: cvandrei
  * since: 2017-01-10
  */
trait ElasticsearchMappings extends ElasticsearchMappingsBase {

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

  final val mappings: Seq[Mapping] = Seq(
    deviceMappings,
    deviceDataRawMappings,
    deviceDataProcessedMappings,
    deviceTypeMappings
  )

}
