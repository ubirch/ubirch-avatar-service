package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw, DeviceDataRawEnvelope}

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerActor extends Actor with ActorLogging {

  override def receive: Receive = {

    case (device: Device, dme: DeviceDataRawEnvelope) =>
      log.debug(s"received device raw data  message: $dme from device: $device")

    case (device: Device, drd: DeviceDataRaw) =>
      log.debug(s"received device raw data message: $drd from device: $device")

    case _ =>
      log.error("received unknown message")
  }
}
