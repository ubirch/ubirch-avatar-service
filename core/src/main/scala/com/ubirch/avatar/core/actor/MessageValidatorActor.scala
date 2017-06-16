package com.ubirch.avatar.core.actor

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.util.model.JsonErrorResponse
import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incoming messages
  */
class MessageValidatorActor(implicit mongo: MongoUtil) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  private val processorActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new MessageProcessorActor())), ActorNames.MSG_PROCESSOR)

  override def receive: Receive = {

    case drd: DeviceDataRaw if drd.v == MessageVersion.v000 =>
      val s = sender()

      log.debug(s"received message with version ${drd.v}")

      DeviceCoreUtil.validateSimpleMessage(hwDeviceId = drd.a).map {
        case Some(dev) =>
          processorActor ! (s, drd, dev)
        case None =>
          s ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"invalid hwDeviceId: ${drd.a}")
      }

    case drd: DeviceDataRaw if drd.v == MessageVersion.v001 =>
      val s = sender()

      log.debug(s"received message with version ${drd.v}")

      DeviceCoreUtil.validateMessage(hwDeviceId = drd.a, authToken = drd.s.getOrElse("nosignature"), payload = drd.p).map {
        case Some(dev) =>
          processorActor ! (s, drd, dev)
        case None =>
          s ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"invalid simple signature: ${drd.a} / ${drd.s}")
      }

    case drd: DeviceDataRaw if drd.v == MessageVersion.v002 || drd.v == MessageVersion.v003 =>
      val s = sender()

      log.debug(s"received message version: ${drd.v}")

      if (drd.k.isDefined)
        DeviceManager.infoByHashedHwId(drd.a).map {
          case Some(dev) =>
            if (DeviceCoreUtil.validateSignedMessage(key = drd.k.getOrElse("nokey"), signature = drd.s.getOrElse("nosignature"), payload = drd.p)) {
              processorActor ! (s, drd, dev)
            }
            else {
              s ! logAndCreateErrorResponse(s"invalid ecc signature: ${drd.a} / ${drd.s}", "ValidationError")
            }
          case None =>
            s ! logAndCreateErrorResponse(s"invalid hwDeviceId: ${drd.a}", "ValidationError")
        }
      else {
        log.error("valid pubKey missing")
        s ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"valid  pubKey missing: ${drd.a} / ${drd.s}")
      }
    case drd: DeviceDataRaw =>
      sender ! logAndCreateErrorResponse(s"received unknown message version: ${drd.v}", "ValidationError")
    case _ =>
      sender ! logAndCreateErrorResponse("received unknown message", "ValidationError")
  }

  private def logAndCreateErrorResponse(msg: String, errType: String): JsonErrorResponse = {
    log.error(msg)
    JsonErrorResponse(errorType = errType, errorMessage = msg)
  }

}
