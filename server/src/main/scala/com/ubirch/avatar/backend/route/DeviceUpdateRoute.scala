package com.ubirch.avatar.backend.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.backend.ResponseUtil
import com.ubirch.avatar.core.actor.MessageValidatorActor
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.device.{DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.model.server.JsonErrorResponse
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceUpdateRoute extends MyJsonProtocol
  with CORSDirective
  with LazyLogging
  with ResponseUtil {

  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(15 seconds)

  private val validatorActor = system.actorOf(Props[MessageValidatorActor], "message-validator")

  val route: Route = {
    path(update) {
      respondWithCORS {
        post {
          entity(as[DeviceDataRaw]) { sdm =>
            onComplete(validatorActor ? sdm) {
              case Success(resp) =>
                resp match {
                  case dm: DeviceStateUpdate =>
                    complete(dm)
                  case jer: JsonErrorResponse =>
                    complete(requestErrorResponse(jer))
                  case _ =>
                    complete(requestErrorResponse(
                      errorType = "UnknownResult",
                      errorMessage = s"received unknown result")
                    )
                }
              case Failure(t) =>
                logger.error("update device data failed", t)
                complete(requestErrorResponse(
                  errorType = "UpdateDeviceError",
                  errorMessage = s"update was not successfull for message ${sdm.id}, error occured: ${t.getMessage}")
                )
            }
          }
        }
      }
    }
  }
}
