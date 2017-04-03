package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.redis.RedisClientUtil
import redis.RedisClient

/**
  * author: cvandrei
  * since: 2016-09-20
  */
class MainRoute {

  val welcome = new WelcomeRoute {}

  val device = new DeviceRoute {}
  //  val deviceUpdate = new DeviceUpdateRoute {}
  val deviceUpdate = new DeviceUpdatePlainRoute {}
  val deviceId = new DeviceIdRoute {}
  val deviceStub = new DeviceStubRoute {}
  val deviceStubId = new DeviceStubIdRoute {}
  val deviceState = new DeviceStateRoute {}
  val deviceDataRaw = new DeviceDataRawRoute {}
  val deviceDataHistory = new DeviceDataHistoryRoute {}
  val deviceType = new DeviceTypeRoute {}

  val forbidden = new ForbiddenRoute {}

  val myRoute: Route = {

    pathPrefix(RouteConstants.apiPrefix) {
      pathPrefix(RouteConstants.serviceName) {
        pathPrefix(RouteConstants.currentVersion) {
          pathPrefix(RouteConstants.device) {
            deviceUpdate.route ~
              deviceType.route ~
              deviceStubId.route ~
              deviceStub.route ~
              deviceState.route ~
              deviceDataHistory.route ~
              deviceDataRaw.route ~
              deviceId.route

          } ~ path(RouteConstants.device) {
            device.route
          } ~
            pathEndOrSingleSlash {
              welcome.route
            }
        }
      }
    } ~
      pathSingleSlash {
        welcome.route
      }
  }
}
