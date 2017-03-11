package com.ubirch.avatar.util.server

import com.ubirch.avatar.config.Config
import com.ubirch.util.elasticsearch.client.binary.config.ESConfig
import com.ubirch.util.elasticsearch.util.{ElasticsearchMappingsBase, IndexInfo, Mapping}

/**
  * author: cvandrei
  * since: 2017-01-10
  */
trait ElasticsearchMappings extends ElasticsearchMappingsBase {

  private val indexInfoDevice = IndexInfo(ESConfig.host, Config.esPortHttp, Config.esDeviceIndex)
  private val indexInfoDeviceRawData = IndexInfo(ESConfig.host, Config.esPortHttp, Config.esDeviceDataRawIndex)
  private val indexInfoDeviceRawDataAnchored = IndexInfo(ESConfig.host, Config.esPortHttp, Config.esDeviceDataRawAnchoredIndex)
  private val indexInfoDeviceHistory = IndexInfo(ESConfig.host, Config.esPortHttp, Config.esDeviceDataProcessedIndex)
  private val indexInfoDeviceType = IndexInfo(ESConfig.host, Config.esPortHttp, Config.esDeviceTypeIndex)
  private val indexInfoAvatarState = IndexInfo(ESConfig.host, Config.esPortHttp, Config.esAvatarStateIndex)

  final val indexInfos: Seq[IndexInfo] = Seq(
    indexInfoDevice,
    indexInfoDeviceRawData,
    indexInfoDeviceRawDataAnchored,
    indexInfoDeviceHistory,
    indexInfoDeviceType,
    indexInfoAvatarState
  )

  private val deviceMappings: Mapping = {

    val mapping =
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
         |        },
         |        "deviceName" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        }
         |      }
         |    }
         |  }
         |}""".stripMargin
    val url = indexInfoDevice.url

    Mapping(url, mapping)

  }

  private val deviceDataRawMappings: Mapping = {

    val mapping =
      s"""{
         |  "mappings": {
         |    "${Config.esDeviceDataRawType}" : {
         |      "properties" : {
         |        "timestamp": {
         |            "type": "date",
         |            "format": "strict_date_optional_time||epoch_millis"
         |        },
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

    Mapping(url, mapping)

  }

  private val deviceDataRawAnchoredMappings: Mapping = {

    val mapping =
      s"""{
         |  "mappings": {
         |    "${Config.esDeviceDataRawAnchoredType}" : {
         |      "properties" : {
         |        "timestamp": {
         |            "type": "date",
         |            "format": "strict_date_optional_time||epoch_millis"
         |        },
         |        "a" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "id" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        },
         |        "deviceName" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        }
         |      }
         |    }
         |  }
         |}""".stripMargin
    val url = indexInfoDeviceRawDataAnchored.url

    Mapping(url, mapping)

  }

  private val deviceDataProcessedMappings: Mapping = {

    val mapping =
      s"""{
         |  "mappings": {
         |    "${Config.esDeviceDataProcessedType}" : {
         |      "properties" : {
         |        "timestamp": {
         |          "type": "date",
         |          "format": "strict_date_optional_time||epoch_millis"
         |        },
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
         |        },
         |        "deviceName" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        }
         |      }
         |    }
         |  }
         |}""".stripMargin
    val url = indexInfoDeviceHistory.url

    Mapping(url, mapping)

  }

  private val deviceTypeMappings: Mapping = {

    val mapping =
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

    Mapping(url, mapping)

  }

  private val avatarStateMappings: Mapping = {

    val mapping =
      s"""{
         |  "mappings": {
         |    "${Config.esAvatarStateType}" : {
         |      "properties" : {
         |        "deviceId" : {
         |          "type" : "string",
         |          "index": "not_analyzed"
         |        }
         |      }
         |    }
         |  }
         |}""".stripMargin
    val url = indexInfoAvatarState.url

    Mapping(url, mapping)

  }

  final val mappings: Seq[Mapping] = Seq(
    deviceMappings,
    deviceDataRawMappings,
    deviceDataRawAnchoredMappings,
    deviceDataProcessedMappings,
    deviceTypeMappings,
    avatarStateMappings
  )

}
