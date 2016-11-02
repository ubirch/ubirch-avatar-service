package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.SimpleDeviceMessage
import com.ubirch.avatar.model.server.JsonErrorResponse

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incomming messages
  */
class MessageValidatorActor extends Actor with ActorLogging {

  implicit val executionContext = context.dispatcher

  private val processorActor = context.actorOf(Props[MessageProcessorActor], "message-processor")

  override def receive: Receive = {
    case sdm: SimpleDeviceMessage if sdm.v == Config.sdmV001 =>
      val s = sender
      log.debug(s"received message with version ${sdm.v}")
      if (sdm.a.isDefined) {
        DeviceManager.infoByHwId(sdm.a.get).map {
          case Some(device) =>
            //DeviceUtil.createSimpleSignature(sdm.p, device)
            processorActor forward sdm
          case None =>
            s ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"invalid hwDeviceId: ${sdm.a.get}")
        }
      }
      else
        s ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"missing hwDeviceId: ${sdm}")

    case dm: SimpleDeviceMessage =>
      log.error(s"received unsupported message version: $dm")
      processorActor forward dm

    case _ =>
      log.error("received unknown message")
  }

}
