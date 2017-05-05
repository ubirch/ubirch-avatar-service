package com.ubirch.avatar.util.server

import com.ubirch.avatar.config.Config
import com.ubirch.util.elasticsearch.util.ElasticsearchMappingsBase

/**
  * author: cvandrei
  * since: 2017-01-10
  */
trait ElasticsearchMappings extends ElasticsearchMappingsBase {

  val indexesAndMappings: Map[String, Map[String, String]] = Map(

    // ubirch-avatar-state
    Config.esAvatarStateIndex -> Map(
      Config.esAvatarStateType ->
        s"""{
           |  "${Config.esAvatarStateType}" : {
           |    "properties" : {
           |      "deviceId" : {
           |        "type" : "keyword"
           |      }
           |    }
           |  }
           |}""".stripMargin
    ),

    // ubirch-device-raw-data
    Config.esDeviceDataRawIndex -> Map(
      Config.esDeviceDataRawType ->
        s"""{
           |  "${Config.esDeviceDataRawType}" : {
           |    "properties" : {
           |      "a" : {
           |        "type" : "keyword"
           |      },
           |      "id" : {
           |        "type" : "keyword"
           |      }
           |    }
           |  }
           |}""".stripMargin
    ),

    // ubirch-device-history
    Config.esDeviceDataHistoryIndex -> Map(
      Config.esDeviceDataHistoryType ->
        s"""{
           |  "${Config.esDeviceDataHistoryType}" : {
           |    "properties" : {
           |      "deviceId" : {
           |        "type" : "keyword"
           |      },
           |      "messageId" : {
           |        "type" : "keyword"
           |      },
           |      "deviceDataRawId" : {
           |        "type" : "keyword"
           |      },
           |      "id" : {
           |        "type" : "keyword"
           |      },
           |      "a" : {
           |        "type" : "keyword"
           |      },
           |      "deviceName" : {
           |        "type" : "keyword"
           |      }
           |    }
           |  }
           |}""".stripMargin
    ),

    // ubirch-device-raw-data-anchored
    Config.esDeviceDataRawAnchoredIndex -> Map(
      Config.esDeviceDataRawAnchoredType ->
        s"""{
           |  "${Config.esDeviceDataRawAnchoredType}" : {
           |    "properties" : {
           |      "a" : {
           |        "type" : "keyword"
           |      },
           |      "id" : {
           |        "type" : "keyword"
           |      },
           |      "deviceName" : {
           |        "type" : "keyword"
           |      }
           |    }
           |  }
           |}""".stripMargin
    ),

    // ubirch-devices
    Config.esDeviceIndex -> Map(
      Config.esDeviceType ->
        s"""{
           |  "${Config.esDeviceType}" : {
           |    "properties" : {
           |      "deviceId" : {
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
           |      }
           |    }
           |  }
           |}""".stripMargin
    ),

    // ubirch-device-state
    Config.esDeviceStateIndex -> Map(
      Config.esDeviceStateType ->
        s"""{
           |  "${Config.esDeviceStateType}" : {
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
           |  }
           |}""".stripMargin
    ),

    // ubirch-device-type
    Config.esDeviceTypeIndex -> Map(
      Config.esDeviceTypeType ->
        s"""{
           |  "${Config.esDeviceTypeType}" : {
           |    "properties" : {
           |      "key" : {
           |        "type" : "keyword"
           |      }
           |    }
           |  }
           |}""".stripMargin
    )

  )

}
