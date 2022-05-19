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
  val deepCheck: ServiceCheckRoute = new ServiceCheckRoute {}
  val backEndInfo: BackendInfoRoute = new BackendInfoRoute {}

  val deviceUpdateMsgPack: DeviceUpdateMsgPackRoute = new DeviceUpdateMsgPackRoute {}
  val deviceClaim: DeviceClaimRoute = new DeviceClaimRoute {}

  val forbidden: ForbiddenRoute = new ForbiddenRoute {}

  val myRoute: Route = {

    pathPrefix(RouteConstants.apiPrefix) {
      pathPrefix(RouteConstants.serviceName) {
        pathPrefix(RouteConstants.currentVersion) {

          pathPrefix(RouteConstants.device) {
            deviceUpdateMsgPack.route ~
              deviceClaim.route

          } ~ path(RouteConstants.check) {
            welcome.route
          } ~ pathEndOrSingleSlash {
            welcome.route
          } ~ deepCheck.route ~
            backEndInfo.route
        }
      }
    } ~ pathSingleSlash {
      welcome.route
    }
  }

}
