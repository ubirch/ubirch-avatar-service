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
    case rdr: DeviceDataRaw if rdr.v == Config.sdmV001 =>
      val s = sender()

      log.debug(s"received message with version ${rdr.v}")


      DeviceUtil.validateMessage(hwDeviceId = rdr.a, authToken = rdr.s, payload = rdr.p).map {
        case Some(dev) =>
          processorActor ! (s, rdr, dev)
          case None =>
            s ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"invalid signature: ${rdr.a} / ${rdr.s}")
        }

    case drd: DeviceDataRaw =>
      val s = sender()
      log.error(s"received message version: $drd")
      processorActor ! (s, drd)
      ???

    case _ =>
      log.error("received unknown message")
  }
}
