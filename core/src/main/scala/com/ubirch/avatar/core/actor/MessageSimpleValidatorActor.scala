package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.SimpleDeviceMessage

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incomming messages
  */
class MessageSimpleValidatorActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case dm: SimpleDeviceMessage if dm.v == Config.sdmV001 =>
      log.debug(s"received message with version ${dm.v}")

    case dm: SimpleDeviceMessage =>
      log.error(s"received unsupported message version: $dm")

    case _ =>
      log.error("received unknown message")
  }

}
