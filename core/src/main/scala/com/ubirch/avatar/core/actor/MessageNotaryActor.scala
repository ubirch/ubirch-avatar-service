package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.model.device.SimpleDeviceMessage

/**
  * Created by derMicha on 28/10/16.
  */
class MessageNotaryActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case sdm: SimpleDeviceMessage =>
      val s = sender
      log.debug(s"received message: $sdm")

    case _ =>
      log.error("received unknown message")
  }

}
