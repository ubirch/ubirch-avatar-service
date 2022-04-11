package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceUpdateJsonRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system:ActorSystem)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val validatorActor = system.actorSelection(ActorNames.MSG_VALIDATOR_PATH)

  val route: Route = {
    path(update / json) {
      post {
        entity(as[DeviceDataRaw]) { ddr =>
          logger.error(s"Disabled Endpoint POST /device/update/json with ddr $ddr was called, though it shouldn't be used anymore")
          complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
        }
      }
    }
  }
}
