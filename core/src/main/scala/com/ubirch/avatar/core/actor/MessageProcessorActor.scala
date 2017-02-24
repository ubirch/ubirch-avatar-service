package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Kill, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.awsiot.util.AwsShadowUtil
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceStateManager
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.transformer.actor.TransformerProducerActor
import com.ubirch.util.json.Json4sUtil

/**
  * author: derMicha
  * since: 2016-10-28
  */
class MessageProcessorActor extends Actor with ActorLogging {

  private val transformerActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[TransformerProducerActor]), ActorNames.TRANSFORMER_PRODUCER)

  private val persistenceActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[MessagePersistenceActor]), ActorNames.PERSISTENCE_SVC)

  private val notaryActor = context.actorOf(Props[MessageNotaryActor], ActorNames.NOTARY_SVC)

  override def receive: Receive = {

    case (s: ActorRef, drd: DeviceDataRaw, device: Device) =>

      log.debug(s"received message: $drd")

      persistenceActor ! drd

      if (DeviceCoreUtil.checkNotaryUsage(device)) //TODO check notary config for device
        notaryActor ! drd

      transformerActor ! drd.id

      //update AWS Shadow state
      AwsShadowUtil.setReported(device, drd.p)

      //send back current device state
      val currentState = DeviceStateManager.currentDeviceState(device)
      s ! currentState

      if (drd.uuid.isDefined) {
        val deviceStateUpdateActor = context.actorOf(DeviceStateUpdateActor.props(drd.uuid.get))
        deviceStateUpdateActor ! Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(currentState).get)
        //deviceStateUpdateActor ! Kill
      }

    case _ => log.error("received unknown message")

  }

}