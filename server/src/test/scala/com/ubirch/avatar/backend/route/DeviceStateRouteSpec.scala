package com.ubirch.avatar.backend.route

import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.mongo.connection.MongoUtil

import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient

/**
  * author: cvandrei
  * since: 2016-10-27
  */
class DeviceStateRouteSpec extends RouteSpec
  with ElasticsearchSpec {

  implicit val ws: WSClient = NingWSClient()
  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.pathDeviceState(":deviceId")}") {

    ignore("deviceId does not exist") {
      // TODO write test
    }

    ignore("deviceId has no state") {
      // TODO write test
    }

    ignore("deviceId has a state") {
      // TODO write test
    }

  }

  feature(s"POST ${RouteConstants.pathDeviceState(":deviceId")}") {

    ignore("deviceId does not exist") {
      // TODO write test
    }

    ignore("input json is invalid") {
      // TODO write test
    }

    ignore("state update is successful") {
      // TODO write test
    }

  }

}
