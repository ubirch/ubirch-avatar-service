package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.model.server.JsonErrorResponse
import com.ubirch.services.util.DeviceUtil

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incomming messages
  */
class MessageValidatorActor extends Actor with ActorLogging {

  implicit val executionContext = context.dispatcher

  private val processorActor = context.actorOf(Props[MessageProcessorActor], "message-processor")

  override def receive: Receive = {

    case drd: DeviceDataRaw if drd.v == Config.sdmV001 =>
      val s = sender()

      log.debug(s"received message with version ${drd.v}")

      DeviceUtil.validateMessage(hwDeviceId = drd.a, authToken = drd.s, payload = drd.p).map {
        case Some(dev) =>
          processorActor ! (s, drd, dev)
        case None =>
          s ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"invalid simple signature: ${drd.a} / ${drd.s}")
      }

    case drd: DeviceDataRaw =>
      val s = sender()

      log.debug(s"received message version: $drd")

      DeviceUtil.validateSignedMessage(hashedHwDeviceId = drd.a, key = drd.k, signature = drd.s, payload = drd.p).map {
        case Some(dev) =>
          processorActor ! (s, drd, dev)
        case None =>
          s ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"invalid ecc signature: ${drd.a} / ${drd.s}")
      }

    case _ =>
      log.error("received unknown message")
  }
}
