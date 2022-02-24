package com.ubirch.avatar.backend.route

import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}
import com.ubirch.avatar.util.server.RouteConstants

/**
  * author: cvandrei
  * since: 2016-10-27
  */
class DeviceStateRouteSpec extends RouteSpec
  with ElasticsearchSpec {


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
