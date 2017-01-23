package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.awsiot.util.AwsShadowUtil
import com.ubirch.avatar.core.device.DeviceStateManager
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.transformer.actor.TransformerProducerActor

/**
  * Created by derMicha on 28/10/16.
  */
class MessageProcessorActor extends Actor with ActorLogging {

  private val transformerActor = context.actorOf(new RoundRobinPool(5).props(Props[TransformerProducerActor]), "transformer-producer")

  private val persistenceActor = context.actorOf(new RoundRobinPool(5).props(Props[MessagePersistorActor]), "persistence-service")

  private val notaryActor = context.actorOf(Props[MessageNotaryActor], "notary-service")

  override def receive: Receive = {
    case (s: ActorRef, drd: DeviceDataRaw, device: Device) =>

      log.debug(s"received message: $drd")

      persistenceActor ! drd

      //TODO check notary config for device
      if (DeviceCoreUtil.checkNotaryUsage(device))
        notaryActor ! drd

      transformerActor ! drd.id

      //update AWS Shadow state
      AwsShadowUtil.setReported(device, drd.p)

      //send back current device state
      s ! DeviceStateManager.currentDeviceState(device)

    case _ =>
      log.error("received unknown message")
  }
}
