package com.ubirch.avatar.backend.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.backend.actor.{CreateDevice, DeviceApiActor}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.Device
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse

import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
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
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val deviceApiActor = system.actorOf(Props[DeviceApiActor], ActorNames.DEVICE_API)

  val route: Route = respondWithCORS {

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
                case _ =>
                  complete("doof")
              }
            case Failure(t) =>
              logger.error("device creation failed", t)
              complete(serverErrorResponse(errorType = "CreationError", errorMessage = t.getMessage))
          }
        }
      }

  }
}
