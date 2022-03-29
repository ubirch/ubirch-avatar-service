package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor

/**
  * author: cvandrei
  * since: 2016-11-02
  */
class DeviceDataRawRoute(implicit httpClient: HttpExt, materializer: Materializer, system: ActorSystem) extends ResponseUtil
  with CORSDirective
  with StrictLogging {


  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val route: Route = {


    path("data" / "reprocess" / Segment) { dayStr =>
      logger.error(s"Disabled endpoint which http method?  device/data/reprocess/$dayStr was called")
      complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage =
        "this endpoint has been disabled together with TrackleService's endpoint /device/data/reprocess"))

    } ~ path(data / raw) {
      post {
        respondWithCORS {
          entity(as[DeviceDataRaw]) { deviceMessage =>

            logger.error(s"Disabled endpoint which http method?  device/data/raw/$deviceMessage was called")
            complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage =
              "this endpoint has been disabled together with TrackleService's endpoint /device/data/raw"))
          }
        }
      }
    } ~ path(data / transferDates / Segment) { deviceId =>
      get {
        logger.error(s"Disabled endpoint GET /data/transferDates/$deviceId was called")
        respondWithCORS {
          complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage =
            "this endpoint has been disabled together with TrackleService's endpoint /device/statistics/create"))
        }
      }
    }
  }

}
