package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.model.util.ErrorFactory
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-11-02
  */
trait DeviceDataRawRoute extends MyJsonProtocol
  with CORSDirective {

  implicit val system = ActorSystem()

  val route: Route = {

    // TODO authentication
    respondWithCORS {

      path(device / data / raw) {

        post {
          entity(as[DeviceDataRaw]) { deviceMessage =>
            onSuccess(DeviceDataRawManager.store(deviceMessage)) {
              case None => complete(errorResponseMessage(deviceMessage))
              case Some(storedMessage) => complete(storedMessage)
            }

          }
        }

      }

    }

  }

  private def errorResponseMessage(deviceMessage: DeviceDataRaw): HttpResponse = {
    val error = ErrorFactory.createString("CreateError", s"failed to persist message")
    HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
  }

}