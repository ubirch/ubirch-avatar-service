package com.ubirch.avatar.core.actor

import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw

import akka.actor.{Actor, ActorLogging}

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incomming messages
  */
class MessageSimpleValidatorActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case dm: DeviceDataRaw if dm.v == MessageVersion.v001 =>
      log.debug(s"received message with version ${dm.v}")

    case dm: DeviceDataRaw =>
      log.error(s"received unsupported message version: $dm")

    case _ =>
      log.error("received unknown message")
  }

}
