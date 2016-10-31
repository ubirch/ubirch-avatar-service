package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.model.device.{Device, SimpleDeviceMessageEnvelope}

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerActor extends Actor with ActorLogging {

  override def receive: Receive = {

    case (device: Device, dme: SimpleDeviceMessageEnvelope) =>
      log.debug(s"received device message: $dme from device: $device")

    case _ =>
      log.error("received unknown message")
  }
}
