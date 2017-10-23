package com.ubirch.avatar.backend.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.{Directives, Route}
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.MessageDeviceCheckActor
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonResponse
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceUpdateBulkRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer)
  extends ResponseUtil
    with Directives
    with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val deviceCheckActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new MessageDeviceCheckActor())), ActorNames.MSG_VALIDATOR)

  val route: Route = {

    path(update / bulk) {

      pathEnd {

        post {

          post {
            entity(as[DeviceDataRaw]) { sdm =>

              deviceCheckActor ! sdm

              complete(JsonResponse(message = "processing started"))
            }
          }
        }
      }
    }
  }
}
