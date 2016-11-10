package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ubirch.avatar.awsiot.util.AwsShadowUtil
import com.ubirch.avatar.core.device.DeviceStateManager
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}

import com.ubirch.transformer.actor.TransformerProducerActor

/**
  * Created by derMicha on 28/10/16.
  */
class MessageProcessorActor extends Actor with ActorLogging {

  private val transformerActor = context.actorOf(Props[TransformerProducerActor], "transformer-producer")

  private val persistorActor = context.actorOf(Props[MessagePersistorActor], "persistor-service")

  private val notaryActor = context.actorOf(Props[MessageNotaryActor], "notatry-service")

  override def receive: Receive = {
    case (s: ActorRef, drd: DeviceDataRaw, device: Device) =>

      log.debug(s"received message: $drd")

      persistorActor ! drd

      //TODO add property check
      notaryActor ! drd

      // TODO AWS State update missing
      transformerActor ! drd.id

      //update AWS Shadow state
      AwsShadowUtil.setReported(device, drd.p)

      //send back current device state
      s ! DeviceStateManager.currentDeviceState(device)

    case _ =>

      log.error("received unknown message")

  }

}
