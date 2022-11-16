package com.ubirch.avatar.util.server

import com.ubirch.avatar.config.Config
import com.ubirch.util.elasticsearch.EsMappingTrait

/**
  * author: cvandrei
  * since: 2017-01-10
  */
trait ElasticsearchMappings extends EsMappingTrait {

  val indexesAndMappings: Map[String, String] = Map(
    // ubirch-devices
    Config.esDeviceIndex ->
      s"""{
         |    "properties" : {
         |      "deviceId" : {
         |        "type" : "keyword"
         |      },
         |      "owners" : {
         |        "type" : "keyword"
         |      },
         |      "groups" : {
         |        "type" : "keyword"
         |      },
         |      "uuid" : {
         |        "type" : "keyword"
         |      },
         |      "hwDeviceId" : {
         |        "type" : "keyword"
         |      },
         |      "hashedHwDeviceId" : {
         |        "type" : "keyword"
         |      },
         |      "deviceName" : {
         |        "type" : "keyword"
         |      },
         |      "created" : {
         |        "type" : "date",
         |        "format" : "strict_date_time"
         |      }
         |    }
         |}""".stripMargin,
    // ubirch-device-state // TODO rename to ubirch-avatar-state-history?
    Config.esDeviceStateIndex ->
      s"""{
         |    "properties" : {
         |      "id" : {
         |        "type" : "keyword"
         |      },
         |      "k" : {
         |        "type" : "keyword"
         |      },
         |      "s" : {
         |        "type" : "keyword"
         |      }
         |    }
         |}""".stripMargin
  )

}
