package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.transformer.actor.TransformerProducerActor

/**
  * Created by derMicha on 28/10/16.
  */
class MessageProcessorActor extends Actor with ActorLogging {

  private val simpleValidatorActor = context.actorOf(Props[MessageSimpleValidatorActor], "simple-validator")
  private val eccValidatorActor = context.actorOf(Props[MessageECCValidatorActor], "ecc-validator")
  private val producerActor = context.actorOf(Props[TransformerProducerActor], "transformer-producer")

  override def receive: Receive = {
    case sdm: DeviceDataRaw if sdm.v == Config.sdmV001 =>
      val s = sender
      log.debug(s"received V0.0.1 message: $sdm")
      simpleValidatorActor ! sdm

      // TODO AWS State update missing
      producerActor ! sdm.id

      s ! sdm
    case sdm: DeviceDataRaw if sdm.v == Config.sdmV002 =>
      val s = sender
      log.debug(s"received V0.0.2 message: $sdm")
      eccValidatorActor ! sdm

      // TODO AWS State update missing
      producerActor ! sdm.id

      s ! sdm
    case sdm: DeviceDataRaw if sdm.v == Config.sdmV003 =>
      val s = sender
      log.debug(s"received V0.0.3 message: $sdm")
      eccValidatorActor ! sdm

      // TODO AWS State update missing
      producerActor ! sdm.id

      s ! sdm
    case _ =>
      log.error("received unknown message")
  }

}
