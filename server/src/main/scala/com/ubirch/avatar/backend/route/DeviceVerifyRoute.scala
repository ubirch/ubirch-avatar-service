/**
  * Copyright (c) 2018 ubirch GmbH
  *
  * @author Matthias L. Jugel
  */
package com.ubirch.avatar.backend.route

import java.util.Base64

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.rest.device.DeviceStateUpdate
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.apache.commons.codec.binary.Hex

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Verify a data hash and return relevant trust information.
  */
class DeviceVerifyRoute(implicit system: ActorSystem) extends ResponseUtil
  with CORSDirective
  with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)
  private val validatorActor = system.actorSelection(ActorNames.MSG_VALIDATOR_PATH)


  val route: Route = {
    path(verify / Segment) { valueHash =>
      respondWithCORS {
        get {
          onComplete(DeviceDataRawManager.loadByValueHash(valueHash)) {
            case Success(Some(deviceDataRaw)) =>
              onComplete(validatorActor ? deviceDataRaw) {
                case Success(resp) if deviceDataRaw.mpraw.isDefined =>
                  resp match {
                    case dm: DeviceStateUpdate if deviceDataRaw.ps.isDefined =>
                      onComplete(DeviceDataRawManager.loadBySignature(deviceDataRaw.ps.get)) {
                        case Success(Some(prev)) =>
                          complete(DeviceVerifyResult(
                            Base64.getEncoder.encodeToString(Hex.decodeHex(deviceDataRaw.mpraw.get)),
                            prev.mpraw.map(raw => Base64.getEncoder.encodeToString(Hex.decodeHex(raw))),
                            deviceDataRaw.txHash.map(AnchorResult(_, deviceDataRaw.txHashLink))
                          ))
                        case _ =>
                          logger.error(s"expected previous message: ${deviceDataRaw.ps}")
                          complete(DeviceVerifyResult(
                            Base64.getEncoder.encodeToString(Hex.decodeHex(deviceDataRaw.mpraw.get)),
                            None,
                            deviceDataRaw.txHash.map(AnchorResult(_, deviceDataRaw.txHashLink))
                          ))
                      }
                    case jer: JsonErrorResponse =>
                      logger.error(jer.errorMessage)
                      complete(StatusCodes.BadRequest -> jer)
                    case _ =>
                      logger.error(s"value hash verification failed: $valueHash")
                      val jer = JsonErrorResponse(errorType = "ValidationError", errorMessage = "value hash verification failed")
                      complete(StatusCodes.BadRequest -> jer)
                  }
                case Failure(e) =>
                  logger.error("value hash verification failed: could not load data", e)
                  complete(StatusCodes.InternalServerError ->
                           JsonErrorResponse(errorType = "internal error", errorMessage = e.getMessage))
              }
            case Success(None)
            =>
              complete(StatusCodes.NotFound,
                JsonErrorResponse(errorType = "validation error", errorMessage = s"unknown hash: $valueHash")
              )
            case Failure(e)
            =>
              logger.error("unable to load device raw data", e)
              complete(StatusCodes.InternalServerError ->
                       JsonErrorResponse(errorType = "internal error", errorMessage = e.getMessage))
          }

        }
      }
    }
  }

}

case class AnchorResult(hash: String, url: Option[String])
case class DeviceVerifyResult(seal: String,
                              chain: Option[String],
                              anchor: Option[AnchorResult])
