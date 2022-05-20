package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.backend.prometheus.ReqMetrics
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.msgpack.MsgPackPacker
import com.ubirch.avatar.model.rest.device.DeviceStateUpdate
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.model.{ JsonErrorResponse, JsonResponse }
import com.ubirch.util.mongo.connection.MongoUtil
import org.apache.commons.codec.binary.Hex

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceUpdateMsgPackRoute()(
  implicit mongo: MongoUtil,
  httpClient: HttpExt,
  materializer: Materializer,
  system: ActorSystem)
  extends ResponseUtil
  with Directives
  with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val msgPackProcessorActor = system
    .actorSelection(ActorNames.MSG_MSGPACK_PROCESSOR_PATH)

  val reqMetrics = new ReqMetrics(metricName = "device_update_mpack")

  val route: Route = {

    path(update / mpack) {
      parameters(Symbol("js") ? false) { js: Boolean =>
        pathEnd {
          reqMetrics.start
          post {
            entity(as[Array[Byte]]) { binData =>
              logger.info(s"POST update/mpack js=$js")
              onComplete(msgPackProcessorActor ? binData) {
                case Success(resp) =>
                  resp match {
                    case dsu: DeviceStateUpdate =>
                      reqMetrics.inc()
                      reqMetrics.stop
                      if (js)
                        complete(StatusCodes.Accepted -> Json4sUtil.any2String(dsu))
                      else {
                        MsgPackPacker.packUbProt(dsu) match {
                          case Right(ubPack) =>
                            logger.debug(
                              s"returning Accepted for POST update/mpack (hex) : ${Hex.encodeHexString(ubPack)}")
                            complete(StatusCodes.Accepted -> ubPack)
                          case Left(_) =>
                            complete(StatusCodes.InternalServerError -> "something went wrong when processing response")
                        }
                      }
                    case jr: JsonResponse =>
                      reqMetrics.incError()
                      reqMetrics.stop
                      logger.info(s"returning Accepted for POST update/mpack ${jr.toJsonString}")
                      complete(StatusCodes.Accepted -> jr.toJsonString)
                    case jer: JsonErrorResponse =>
                      reqMetrics.incError()
                      reqMetrics.stop
                      logger.info(s"returning Bad Request for POST update/mpack ${jer.toJsonString}")
                      complete(StatusCodes.BadRequest -> jer.toJsonString)
                    case _ =>
                      reqMetrics.inc()
                      reqMetrics.stop
                      val jer = JsonErrorResponse(errorType = "response error", errorMessage = "ERROR 1: no result")
                      logger.info(s"returning Internal Server Error for POST ${jer.toJsonString}")
                      complete(StatusCodes.InternalServerError -> jer.toJsonString)
                  }
                case Failure(t) =>
                  reqMetrics.incError()
                  reqMetrics.stop
                  logger.error("got no result", t)
                  val jer = JsonErrorResponse(errorType = "internal error", errorMessage = t.getMessage)
                  logger.info(s"returning Internal Server Error for POST ${jer.toJsonString}")
                  complete(StatusCodes.InternalServerError -> jer.toJsonString)
              }
            }
          }
        }
      }
    }
  }
}
