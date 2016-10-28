package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}

/**
  * Created by derMicha on 28/10/16.
  */
class MessagePersistorActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case _ =>
      log.error("received unknown message")
  }

}
