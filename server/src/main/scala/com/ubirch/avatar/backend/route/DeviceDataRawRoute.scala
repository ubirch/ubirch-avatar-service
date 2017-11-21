package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.backend.prometheus.ReqCounter
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import io.prometheus.client.Counter

import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-11-02
  */
class DeviceDataRawRoute(implicit system:ActorSystem) extends ResponseUtil
  with CORSDirective {

  val reqCounter = new ReqCounter("device_update_raw")

  val route: Route = {

    // TODO authentication

    path(data / raw) {
      post {
        respondWithCORS {
          entity(as[DeviceDataRaw]) { deviceMessage =>

            onComplete(DeviceDataRawManager.store(deviceMessage)) {

              case Success(res) => res match {
                case None =>
                  reqCounter.requestsErrors.inc
                  complete(requestErrorResponse("CreateError", s"failed persist message: $deviceMessage"))
                case Some(storedMessage) =>
                  reqCounter.requests.inc()
                  complete(storedMessage)
              }

              case Failure(t) =>
                reqCounter.requestsErrors.inc
                complete(requestErrorResponse("CreateError", s"failed persist message: $deviceMessage"))

            }

          }
        }
      }
    }

  }

}
