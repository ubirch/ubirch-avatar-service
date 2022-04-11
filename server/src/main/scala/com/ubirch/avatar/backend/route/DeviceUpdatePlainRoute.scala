package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.backend.prometheus.ReqMetrics
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants.update
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 26/02/17.
  */
class DeviceUpdatePlainRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system: ActorSystem)
  extends ResponseUtil
    with MyJsonProtocol
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val validatorActor = system.actorSelection(ActorNames.MSG_VALIDATOR_PATH)

  val reqMetrics = new ReqMetrics("device_update_plain")

  val route: Route = {
    path(update) {
      pathEnd {
        reqMetrics.start
        post {
          entity(as[String]) { ddrString =>
            logger.error(s"Disabled Endpoint POST /device/update with ddr $ddrString was called, though it shouldn't be used anymore")
            complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
          }
        }
      }
    }
  }
}
