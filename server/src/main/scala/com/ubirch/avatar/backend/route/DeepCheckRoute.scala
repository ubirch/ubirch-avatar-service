package com.ubirch.avatar.backend.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.DeepCheckActor
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.deepCheck.model.{DeepCheckRequest, DeepCheckResponse}
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-06-08
  */
class DeepCheckRoute(implicit mongo: MongoUtil, _system: ActorSystem, httpClient: HttpExt, materializer: Materializer) extends CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = _system.dispatcher
  implicit val timeout: Timeout = Timeout(5 * Config.actorTimeout seconds)

  private val deepCheckActor = _system.actorOf(new RoundRobinPool(Config.akkaNumberOfBackendWorkers).props(Props(new DeepCheckActor())), ActorNames.DEEP_CHECK)

  val route: Route = {

    path(RouteConstants.deepCheck) {
      respondWithCORS {
        get {

          onComplete(deepCheckActor ? DeepCheckRequest()) {

            case Failure(t) =>
              logger.error(s"failed to run deepCheck: ${t.getMessage}", t)
              complete(serverErrorResponse(errorType = "ServerError", errorMessage = s"sorry, something went wrong on our end: ${t.getMessage}"))

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
    }

  }

}
