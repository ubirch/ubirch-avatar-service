package com.ubirch.avatar.util.server

import co.elastic.clients.elasticsearch._types.mapping.{ DateProperty, GeoPointProperty, KeywordProperty, Property }
import com.ubirch.avatar.config.Config
import com.ubirch.util.elasticsearch.EsMappingTrait

/**
  * author: cvandrei
  * since: 2017-01-10
  */
trait ElasticsearchMappings extends EsMappingTrait {

  val indexesAndMappings: Map[String, Map[String, Property]] = Map(
    Config.esDeviceIndex -> Map(
      "deviceId" -> keyWordProperty,
      "owners" -> keyWordProperty,
      "groups" -> keyWordProperty,
      "uuid" -> keyWordProperty,
      "hwDeviceId" -> keyWordProperty,
      "hashedHwDeviceId" -> keyWordProperty,
      "deviceName" -> keyWordProperty,
      "created" -> strictDateTimeProperty
    ),
    Config.esDeviceStateIndex -> Map(
      "id" -> keyWordProperty,
      "k" -> keyWordProperty,
      "s" -> keyWordProperty
    )
  )

  private def strictDateTimeProperty: Property = {
    new DateProperty.Builder().format("strict_date_time").build()._toProperty()
  }

  private def keyWordProperty: Property = {
    new KeywordProperty.Builder().build()._toProperty()
  }
}
