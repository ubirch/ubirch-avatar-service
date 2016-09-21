package com.ubirch.avatar.backend.route

import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.Welcome
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceRoute extends MyJsonProtocol
  with CORSDirective {

  val route: Route = {

    respondWithCORS {
      path(device) {

        get {
          // TODO actual implementation: call DeviceRouteUtil (in module core)
          complete(Welcome(message = s"GET ${RouteConstants.urlDevice}"))
        } ~
          post {
            // TODO actual implementation: call DeviceRouteUtil (in module core)
            complete(Welcome(message = s"POST ${RouteConstants.urlDevice}"))
          }

      }
    }

  }

}
