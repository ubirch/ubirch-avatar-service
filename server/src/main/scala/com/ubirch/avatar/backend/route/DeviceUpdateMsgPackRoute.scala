package com.ubirch.avatar.backend.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.backend.prometheus.ReqMetrics
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.MessageMsgPackProcessorActor
import com.ubirch.avatar.model.rest.device.DeviceStateUpdate
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.model.{JsonErrorResponse, JsonResponse}
import com.ubirch.util.mongo.connection.MongoUtil
import io.prometheus.client.Histogram

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceUpdateMsgPackRoute()(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system: ActorSystem)
  extends ResponseUtil
    with Directives
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val msgPackProcessorActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new MessageMsgPackProcessorActor())), ActorNames.MSG_MSGPACK_PROCESSOR)

  val reqMetrics = new ReqMetrics(metricName = "device_update_mpack")

  val route: Route = {

    path(update / mpack) {

      pathEnd {
        reqMetrics.start
        post {
          entity(as[Array[Byte]]) { binData =>
            onComplete(msgPackProcessorActor ? binData) {
              case Success(resp) =>
                resp match {
                  case dsu: DeviceStateUpdate =>

                    val dsuJson = Json4sUtil.any2jvalue(dsu).get
                    val dsuString = Json4sUtil.jvalue2String(dsuJson)
                    reqMetrics.inc
                    reqMetrics.stop
                    complete(dsuString)
                  case jRepsonse: JsonResponse =>
                    reqMetrics.incError
                    reqMetrics.stop
                    complete(jRepsonse.toJsonString)
                  case jErrorRepsonse: JsonErrorResponse =>
                    reqMetrics.incError
                    reqMetrics.stop
                    complete(jErrorRepsonse.toJsonString)
                  case _ =>
                    reqMetrics.inc
                    reqMetrics.stop
                    complete(s"ERROR 1: invlaid response")
                }
              case Failure(t) =>
                reqMetrics.incError
                reqMetrics.stop
                logger.error("got no result", t)
                complete(s"ERROR 2: no result")
            }


          }
        }
      }
    }
  }
}
