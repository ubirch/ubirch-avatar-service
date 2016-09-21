package com.ubirch.avatar.backend.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.server.util.RouteConstants._

/**
  * author: cvandrei
  * since: 2016-09-20
  */
class MainRoute {

  val welcome = new WelcomeRoute {}

  val device = new DeviceRoute {}
  val deviceId = new DeviceIdRoute {}
  val deviceStubId = new DeviceStubIdRoute {}

  val myRoute: Route = {

    pathPrefix(api) {
      pathPrefix(v1) {
        pathPrefix(avatarService) {

          device.route ~
            deviceId.route ~
            deviceStubId.route

        }
      }
    } ~
      pathSingleSlash {
        welcome.route
      }

  }

}
