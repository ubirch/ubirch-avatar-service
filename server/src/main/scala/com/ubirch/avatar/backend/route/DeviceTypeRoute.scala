package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.model.rest.device.DeviceType
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-11-09
  */
class DeviceTypeRoute(implicit httpClient: HttpExt, materializer: Materializer, system:ActorSystem) extends ResponseUtil
  with CORSDirective
  with StrictLogging
   {


  val route: Route = {

    path(RouteConstants.deviceType) {
      respondWithCORS {

        get {
          logger.error("Disabled Endpoint GET /device/deviceType was called, though it shouldn't be used anymore")
          complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
        }
        } ~ post {
          entity(as[DeviceType]) { postDeviceType =>
            logger.error(s"Disabled Endpoint POST /device/deviceType with deviceType $postDeviceType was called, though it shouldn't be used anymore")
            complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
          }
        } ~ put {
          entity(as[DeviceType]) { postDeviceType =>
            logger.error(s"Disabled Endpoint PUT /device/deviceType with deviceType $postDeviceType was called, though it shouldn't be used anymore")
            complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
          }
        }
      }
    } ~ path(RouteConstants.deviceType / RouteConstants.init) {
    respondWithCORS {
      get {
        logger.error("Disabled Endpoint GET /device/deviceType/init was called, though it shouldn't be used anymore")
        complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
      }
    }
  }
}
