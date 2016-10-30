package com.ubirch.avatar.backend.route


import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.backend.Actor.{CreateDevice, DeviceApiActor}
import com.ubirch.avatar.backend.ResponseUtil
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.device.Device
import com.ubirch.avatar.model.server.JsonErrorResponse
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceRoute extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with LazyLogging {

  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(15 seconds)

  private val deviceApiActor = system.actorOf(Props[DeviceApiActor], "device-api")

  val route: Route = respondWithCORS {
    path(device) {

      // TODO authentication for all methods...or just for post?
      get {
        complete(DeviceManager.all())
      } ~
        post {

          entity(as[Device]) { device =>
            onComplete(deviceApiActor ? CreateDevice(device = device)) {
              case Success(resp) =>
                resp match {
                  case dev: Device =>
                    complete(dev)
                  case jer: JsonErrorResponse =>
                    complete(requestErrorResponse(jer))
                }
              case Failure(t) =>
                logger.error("device creation failed", t)
                complete(serverErrorResponse(errorType = "CreationError", errorMessage = t.getMessage))
            }
          }
        }
    }
  }
}
