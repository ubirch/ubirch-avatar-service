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
  val deviceUpdate = new DeviceUpdateRoute {}
  val deviceId = new DeviceIdRoute {}
  val deviceStub = new DeviceStubRoute {}
  val deviceStubId = new DeviceStubIdRoute {}
  val deviceState = new DeviceStateRoute {}
  val deviceDataRaw = new DeviceDataRawRoute {}
  val deviceDataHistory = new DeviceDataHistoryRoute {}

  val myRoute: Route = {

    pathPrefix(apiPrefix) {
      pathPrefix(serviceName) {
        pathPrefix(currentVersion) {

          deviceUpdate.route ~
            deviceStubId.route ~
            deviceStub.route ~
            deviceState.route ~
            deviceId.route ~
            device.route ~
            deviceDataRaw.route ~
            deviceDataHistory.route


        }
      }
    } ~
      pathSingleSlash {
        welcome.route
      }

  }

}
