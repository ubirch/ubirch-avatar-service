package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.backend.actor.HistoryActor
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceHistoryManager
import com.ubirch.avatar.model.rest.device.DeviceHistory
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-30
  */
class DeviceDataHistoryRoute(implicit system: ActorSystem) extends ResponseUtil
  with CORSDirective
  with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)
  val route: Route = {


    pathPrefix(JavaUUID / data) { deviceId =>

      path(history) {
        respondWithCORS {
          get {
            logger.error(s"Disabled Endpoint GET device/$deviceId/data/history was called, though it shouldn't be used anymore")
            complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
          }
        }
      } ~ pathPrefix(history) {

        path(IntNumber) { from: Int =>
          respondWithCORS {
            get {
              logger.error(s"Disabled Endpoint GET device/$deviceId/data/history/$from was called, though it shouldn't be used anymore")
              complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
            }
          }
        } ~ path(IntNumber / IntNumber) { (from, size) =>
          respondWithCORS {
            get {
              logger.error(s"Disabled Endpoint GET device/$deviceId/data/history/$from/$size was called, though it shouldn't be used anymore")
              complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
            }
          }
        } ~ pathPrefix(byDate) {
          path(from / Segment / to / Segment) { (fromString, toString) =>
            logger.error(s"Disabled Endpoint GET device/$deviceId/data/byDate/$fromString/$toString was called, though it shouldn't be used anymore")
            complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))

          }
        } ~ path(before / Segment) { beforeString =>
          respondWithCORS {
            get {
              logger.error(s"Disabled Endpoint GET device/$deviceId/data/before/$beforeString was called, though it shouldn't be used anymore")
              complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))

            }
          }
        } ~ path(after / Segment) { afterString =>
          respondWithCORS {
            get {
              logger.error(s"Disabled Endpoint GET device/$deviceId/data/after/$afterString was called, though it shouldn't be used anymore")
              complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))

            }
          }
        } ~ path(day / Segment) { dayString =>
          respondWithCORS {
            get {
              logger.error(s"Disabled Endpoint GET device/$deviceId/data/day/$dayString was called, though it shouldn't be used anymore")
              complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))

            }
          }
        }

      }
    }
  }
  private val historyActor = system.actorOf(HistoryActor.props, ActorNames.HISTORY)

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
