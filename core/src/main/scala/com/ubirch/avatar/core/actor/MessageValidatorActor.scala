package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.model.device.SimpleDeviceMessage

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incomming messages
  */
class MessageValidatorActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case dm: SimpleDeviceMessage if dm.v == "0.0.1" =>
      log.debug("received message with version 0.0.1")
    case dm: SimpleDeviceMessage if dm.v == "0.0.2" =>
      log.debug("received message with version 0.0.2")
    case dm: SimpleDeviceMessage if dm.v == "0.0.3" =>
      log.debug("received message with version 0.0.3")
    case _ =>
      log.error("received unknown message")
  }

}
