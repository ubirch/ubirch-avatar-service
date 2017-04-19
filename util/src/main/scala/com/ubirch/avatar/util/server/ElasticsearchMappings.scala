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
           |        "type" : "string",
           |        "index": "not_analyzed"
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
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "id" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      }
           |    }
           |  }
           |}""".stripMargin
    ),

    // ubirch-device-history
    Config.esDeviceDataProcessedIndex -> Map(
      Config.esDeviceDataProcessedType ->
        s"""{
           |  "${Config.esDeviceDataProcessedType}" : {
           |    "properties" : {
           |      "deviceId" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "messageId" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "deviceDataRawId" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "id" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "a" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "deviceName" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
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
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "id" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "deviceName" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
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
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "hwDeviceId" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "hashedHwDeviceId" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "deviceName" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
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
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "k" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      },
           |      "s" : {
           |        "type" : "string",
           |        "index": "not_analyzed"
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
           |        "type" : "string",
           |        "index": "not_analyzed"
           |      }
           |    }
           |  }
           |}""".stripMargin
    )

  )

}
