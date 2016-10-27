package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.server.util.RouteConstants._

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  * author: cvandrei
  * since: 2016-09-20
  */
class MainRoute {

  val welcome = new WelcomeRoute {}

  val device = new DeviceRoute {}
  val deviceId = new DeviceIdRoute {}
  val deviceStubId = new DeviceStubIdRoute {}
  val deviceState = new DeviceStateRoute {}
  val deviceDataHistory = new DeviceMessageRoute {}

  val myRoute: Route = {

    pathPrefix(apiPrefix) {
      pathPrefix(serviceName) {
        pathPrefix(currentVersion) {

          device.route ~
            deviceId.route ~
            deviceStubId.route ~
            deviceState.route ~
            deviceDataHistory.route

        }
      }
    } ~
      pathSingleSlash {
        welcome.route
      }

  }

}
