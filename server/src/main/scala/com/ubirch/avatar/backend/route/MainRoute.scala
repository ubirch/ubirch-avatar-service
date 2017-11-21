package com.ubirch.avatar.backend.route

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.mongo.connection.MongoUtil
import akka.actor.ActorSystem
import io.prometheus.client.Counter

class AllCounter {
  val requests: Counter = Counter.build()
    .name("requests_total")
    .help("Total msgPack device updates")
//    .labelNames("deviceUpdateMsgPack")
    .register()

  val requestsErrors: Counter = Counter.build()
    .name("requests_failed_total")
    .help("Total msgPack device update errors")
//    .labelNames("deviceUpdateMsgPackErrors")
    .register()
}

/**
  * author: cvandrei
  * since: 2016-09-20
  */
class MainRoute(implicit mongo: MongoUtil, _system: ActorSystem, httpClient: HttpExt, materializer: Materializer) {

  implicit val allCounter = new AllCounter

  val welcome = new WelcomeRoute {}
  val deepCheck = new DeepCheckRoute {}

  val device = new DeviceRoute {}
  val deviceUpdatePlain = new DeviceUpdatePlainRoute {}
  val deviceUpdateBulk = new DeviceUpdateBulkRoute {}
  val deviceUpdateMsgPack = new DeviceUpdateMsgPackRoute {}
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
            deviceUpdateBulk.route ~
              deviceUpdateMsgPack.route ~
              deviceUpdatePlain.route ~
              deviceType.route ~
              deviceStubId.route ~
              deviceStub.route ~
              deviceState.route ~
              deviceDataHistory.route ~
              deviceDataRaw.route ~
              deviceId.route

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
