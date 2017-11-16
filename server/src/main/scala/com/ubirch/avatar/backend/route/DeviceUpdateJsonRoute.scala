package com.ubirch.avatar.backend.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.MessageValidatorActor
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.{JsonErrorResponse, JsonResponse}
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceUpdateJsonRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system:ActorSystem)
  extends ResponseUtil
  with CORSDirective
  with StrictLogging  {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val validatorActor = system.actorSelection(ActorNames.MSG_VALIDATOR_PATH)

  private val oidcDirective = new OidcDirective()

  val route: Route = {
    path(update / json) {
      post {
        entity(as[DeviceDataRaw]) { ddr =>
          onComplete(validatorActor ? ddr) {
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
                errorMessage = s"update failed for message ${ddr.id}, error occurred: ${t.getMessage.replace("\"", "'")}")
              )
          }
        }
      }
    }
  }
}
