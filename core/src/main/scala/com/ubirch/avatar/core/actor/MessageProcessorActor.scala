package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.transformer.actor.TransformerProducerActor

/**
  * Created by derMicha on 28/10/16.
  */
class MessageProcessorActor extends Actor with ActorLogging {

  private val producerActor = context.actorOf(Props[TransformerProducerActor], "transformer-producer")

  private val persistorActor = context.actorOf(Props[MessagePersistorActor], "persistor-service")

  private val notaryActor = context.actorOf(Props[MessageNotaryActor], "notatry-service")

  override def receive: Receive = {
    case sdm: DeviceDataRaw =>
      val s = sender
      log.debug(s"received message: $sdm")
      //TODO add property check
      persistorActor ! sdm
      //TODO add property check
      notaryActor ! sdm

      // TODO AWS State update missing
      producerActor ! sdm.id

      // TODO AWS State update missing
      s ! sdm

    case _ =>

      log.error("received unknown message")

  }

}
