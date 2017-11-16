package com.ubirch.avatar.backend.route

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.backend.actor.{HistoryActor, HistoryAfter, HistoryBefore, HistoryByDate, HistoryByDay, HistorySeq}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceHistoryManager
import com.ubirch.avatar.model.rest.device.DeviceHistory
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective

import org.joda.time.DateTime

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-30
  */
class DeviceDataHistoryRoute(implicit system:ActorSystem) extends ResponseUtil
  with CORSDirective
  with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val historyActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[HistoryActor]), ActorNames.HISTORY)

  val route: Route = {

    // TODO authentication

    pathPrefix(JavaUUID / data) { deviceId =>

      path(history) {
        respondWithCORS {
          get {
            onSuccess(queryHistory(deviceId)) { deviceData =>
              complete(deviceData)
            }
          }
        }

      } ~ pathPrefix(history) {

        path(IntNumber) { from =>
          respondWithCORS {
            get {
              onSuccess(queryHistory(deviceId, Some(from))) { deviceData =>
                complete(deviceData)
              }
            }
          }

        } ~ path(IntNumber / IntNumber) { (from, size) =>

          respondWithCORS {
            get {
              onSuccess(queryHistory(deviceId, Some(from), Some(size))) { deviceData =>
                complete(deviceData)
              }
            }

          }

        } ~ pathPrefix(byDate) {

          path(from / Segment / to / Segment) { (fromString, toString) =>
            // TODO automated tests
            respondWithCORS {
              get {

                val from = DateTime.parse(fromString)
                val to = DateTime.parse(toString)
                onComplete(historyActor ? HistoryByDate(deviceId, from, to)) {

                  case Failure(t) => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "querying device history by date failed"))

                  case Success(resp) =>
                    resp match {
                      case seq: HistorySeq => complete(seq.seq)
                      case _ =>
                        logger.error("querying device history by date resulted in unknown message")
                        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "querying device history by date failed"))
                    }

                }

              }
            }
          } ~ path(before / Segment) { beforeString =>
            // TODO automated tests
            respondWithCORS {
              get {

                val before = DateTime.parse(beforeString)
                onComplete(historyActor ? HistoryBefore(deviceId, before)) {

                  case Failure(t) => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "querying device history byDate/before failed"))

                  case Success(resp) =>
                    resp match {
                      case seq: HistorySeq => complete(seq.seq)
                      case _ =>
                        logger.error("querying device history byDate/before resulted in unknown message")
                        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "querying device history byDate/before failed"))
                    }

                }

              }
            }
          } ~ path(after / Segment) { afterString =>
            // TODO automated tests
            respondWithCORS {
              get {

                val after = DateTime.parse(afterString)
                onComplete(historyActor ? HistoryAfter(deviceId, after)) {

                  case Failure(t) => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "querying device history byDate/after failed"))

                  case Success(resp) =>
                    resp match {
                      case seq: HistorySeq => complete(seq.seq)
                      case _ =>
                        logger.error("querying device history/after resulted in unknown message")
                        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "querying device history byDate/after failed"))
                    }

                }

              }
            }
          } ~ path(day / Segment) { dayString =>
            // TODO automated tests
            respondWithCORS {
              get {

                val day = DateTime.parse(dayString)
                onComplete(historyActor ? HistoryByDay(deviceId, day)) {

                  case Failure(t) => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "querying device history byDate/day failed"))

                  case Success(resp) =>
                    resp match {
                      case seq: HistorySeq => complete(seq.seq)
                      case _ =>
                        logger.error("querying device history/day resulted in unknown message")
                        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "querying device history byDate/day failed"))
                    }

                }

              }
            }
          }

        }

      }

    }

  }

  //TODO refactor this, put it into an actor
  private def queryHistory(deviceId: UUID,
                           fromOpt: Option[Int] = None,
                           sizeOpt: Option[Int] = None
                          ): Future[Seq[DeviceHistory]] = {

    fromOpt match {

      case Some(fromInt) =>
        sizeOpt match {
          case Some(size) => DeviceHistoryManager.history(deviceId.toString, fromInt, size)
          case None => DeviceHistoryManager.history(deviceId.toString, fromInt)
        }

      case None => DeviceHistoryManager.history(deviceId.toString)

    }

  }

}
