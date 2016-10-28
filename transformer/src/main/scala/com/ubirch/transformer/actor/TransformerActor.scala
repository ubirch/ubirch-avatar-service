package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging}

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case _ =>
      log.error("received unknown message")
  }
}
