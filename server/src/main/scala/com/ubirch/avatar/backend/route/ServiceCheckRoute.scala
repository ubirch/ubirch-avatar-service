package com.ubirch.avatar.backend.route

import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.ServcieCheckActor
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.avatar.util.server.RouteConstants.readyCheck
import com.ubirch.util.deepCheck.model._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

/**
  * author: cvandrei
  * since: 2017-06-08
  */
class ServiceCheckRoute(
  implicit mongo: MongoUtil,
  _system: ActorSystem,
  httpClient: HttpExt,
  materializer: Materializer)
  extends CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = _system.dispatcher
  implicit val timeout: Timeout = Timeout(5 * Config.actorTimeout seconds)

  private val serviceCheckActor = _system.actorOf(
    new RoundRobinPool(Config.akkaNumberOfBackendWorkers).props(Props(new ServcieCheckActor())),
    ActorNames.DEEP_CHECK)

  val route: Route = {

    path(RouteConstants.deepCheck) {
      respondWithCORS {
        get {

          onComplete(serviceCheckActor ? DeepCheckRequest()) {

            case Failure(t) =>
              logger.error(s"failed to run deepCheck: ${t.getMessage}", t)
              complete(serverErrorResponse(
                errorType = "ServerError",
                errorMessage = s"sorry, something went wrong on our end: ${t.getMessage}"))

            case Success(resp) =>
              resp match {

                case res: DeepCheckResponse if res.status =>
                  complete(StatusCodes.OK -> res)
                case res: DeepCheckResponse if !res.status =>
                  complete(response(responseObject = res, status = StatusCodes.ServiceUnavailable))
                case jre: JsonErrorResponse =>
                  complete(StatusCodes.InternalServerError -> jre)
                case _ =>
                  complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to run deep check"))

              }

          }

        }
      }
    } ~
      path(readyCheck) {
        respondWithCORS {
          get {

            onComplete(serviceCheckActor ? ReadyCheckRequest()) {

              case Failure(t) =>
                logger.error(s"failed to run readyCheck: ${t.getMessage}", t)
                complete(serverErrorResponse(
                  errorType = "ServerError",
                  errorMessage = s"sorry, something went wrong on our end: ${t.getMessage}"))

              case Success(resp) =>
                resp match {

                  case res: DeepCheckResponse if res.status =>
                    complete(StatusCodes.OK -> res)
                  case res: DeepCheckResponse if !res.status =>
                    complete(response(responseObject = res, status = StatusCodes.ServiceUnavailable))
                  case jre: JsonErrorResponse =>
                    complete(StatusCodes.InternalServerError -> jre)
                  case _ =>
                    complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to run readyCheck"))

                }

            }

          }
        }
      } ~
      path(RouteConstants.liveCheck) {
        respondWithCORS {
          get {

            onComplete(serviceCheckActor ? LiveCheckRequest()) {

              case Failure(t) =>
                logger.error(s"failed to run liveCheck: ${t.getMessage}", t)
                complete(serverErrorResponse(
                  errorType = "ServerError",
                  errorMessage = s"sorry, something went wrong on our end: ${t.getMessage}"))

              case Success(resp) =>
                resp match {

                  case res: ServiceCheckResponse if res.status =>
                    complete(StatusCodes.OK -> res)
                  case res: ServiceCheckResponse if !res.status =>
                    complete(response(responseObject = res, status = StatusCodes.ServiceUnavailable))
                  case jre: JsonErrorResponse =>
                    complete(StatusCodes.InternalServerError -> jre)
                  case _ =>
                    complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to run liveCheck"))

                }

            }

          }
        }
      }

  }

}
