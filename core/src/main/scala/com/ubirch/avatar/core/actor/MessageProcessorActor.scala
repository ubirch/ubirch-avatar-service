package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceStateManager
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.services.util.DeviceUtil
import com.ubirch.transformer.actor.TransformerProducerActor

/**
  * Created by derMicha on 28/10/16.
  */
class MessageProcessorActor extends Actor with ActorLogging {

  private val producerActor = context.actorOf(Props[TransformerProducerActor], "transformer-producer")

  private val persistorActor = context.actorOf(Props[MessagePersistorActor], "persistor-service")

  private val notaryActor = context.actorOf(Props[MessageNotaryActor], "notatry-service")

  override def receive: Receive = {
    case (s: ActorRef, sdm: DeviceDataRaw, device: Device) =>

      log.debug(s"received message: $sdm")

      //TODO add property check
      persistorActor ! sdm

      //TODO add property check
      notaryActor ! sdm

      // TODO AWS State update missing
      producerActor ! sdm.id

      // TODO AWS State update missing
      s ! DeviceStateManager.currentDeviceState(device)

    case _ =>

      log.error("received unknown message")

  }

}
