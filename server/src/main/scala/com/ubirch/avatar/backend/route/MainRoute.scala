package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.mongo.connection.MongoUtil

/**
  * author: cvandrei
  * since: 2016-09-20
  */
class MainRoute(implicit mongo: MongoUtil, _system: ActorSystem, httpClient: HttpExt, materializer: Materializer) {

  val welcome: WelcomeRoute = new WelcomeRoute {}
  val deepCheck: DeepCheckRoute = new DeepCheckRoute {}

  val device: DeviceRoute = new DeviceRoute {}
  val deviceUpdatePlain: DeviceUpdatePlainRoute = new DeviceUpdatePlainRoute {}
  val deviceUpdateBulk: DeviceUpdateBulkRoute = new DeviceUpdateBulkRoute {}
  val deviceUpdateMsgPack: DeviceUpdateMsgPackRoute = new DeviceUpdateMsgPackRoute {}
  val deviceId: DeviceIdRoute = new DeviceIdRoute {}
  val deviceStub: DeviceStubRoute = new DeviceStubRoute {}
  val deviceStubId: DeviceStubIdRoute = new DeviceStubIdRoute {}
  val deviceState: DeviceStateRoute = new DeviceStateRoute {}
  val deviceDataRaw: DeviceDataRawRoute = new DeviceDataRawRoute {}
  val deviceDataHistory: DeviceDataHistoryRoute = new DeviceDataHistoryRoute {}
  val deviceVerify: DeviceVerifyRoute = new DeviceVerifyRoute {}
  val deviceType: DeviceTypeRoute = new DeviceTypeRoute {}
  val deviceClaim: DeviceClaimRoute = new DeviceClaimRoute {}

  val forbidden: ForbiddenRoute = new ForbiddenRoute {}

  val myRoute: Route = {

    pathPrefix(RouteConstants.apiPrefix) {
      pathPrefix(RouteConstants.serviceName) {
        pathPrefix(RouteConstants.currentVersion) {

          pathPrefix(RouteConstants.device) {
            deviceUpdateBulk.route ~
              deviceUpdateMsgPack.route ~
              deviceUpdatePlain.route ~
              deviceVerify.route ~
              deviceType.route ~
              deviceStubId.route ~
              deviceStub.route ~
              deviceState.route ~
              deviceDataHistory.route ~
              deviceDataRaw.route ~
              deviceId.route ~
              deviceClaim.route

          } ~ path(RouteConstants.device) {
            device.route
          } ~ path(RouteConstants.check) {
            welcome.route
          } ~ pathEndOrSingleSlash {
            welcome.route
          } ~ deepCheck.route

        }
      }
    } ~ pathSingleSlash {
      welcome.route
    }
  }
}
