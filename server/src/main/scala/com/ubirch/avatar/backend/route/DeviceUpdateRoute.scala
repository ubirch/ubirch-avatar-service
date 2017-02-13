package com.ubirch.avatar.backend.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.core.actor.MessageValidatorActor
import com.ubirch.avatar.model.device.{DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.{JsonErrorResponse, JsonResponse}

import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceUpdateRoute extends MyJsonProtocol
  with CORSDirective
  with StrictLogging
  with ResponseUtil {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(15 seconds)

  private val validatorActor = system.actorOf(new RoundRobinPool(3).props(Props[MessageValidatorActor]), "message-validator")

  val route: Route = {
    path(update) {
      respondWithCORS {
        post {
          entity(as[DeviceDataRaw]) { sdm =>
            onComplete(validatorActor ? sdm) {
              case Success(resp) =>
                resp match {
                  case dm: DeviceStateUpdate => complete(dm)
                  case jer: JsonErrorResponse => complete(requestErrorResponse(jer))
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
                  errorMessage = s"update failed for message ${sdm.id}, error occured: ${t.getMessage.replace("\"", "'")}")
                )
            }
          }
        }
      }
    } ~
      path(bulk) {
        respondWithCORS {
          post {
            entity(as[DeviceDataRaw]) { sdm =>
              validatorActor ! sdm
              complete(JsonResponse(message = "processing started"))
            }
          }
        }
      }
  }
}
