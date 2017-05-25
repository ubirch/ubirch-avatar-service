package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Kill, Props}
import akka.camel.CamelMessage
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceStateManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.actors.MessageReceiver
import com.ubirch.avatar.model.rest.device.{Device, DeviceDataRaw}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.transformer.actor.TransformerProducerActor
import com.ubirch.util.json.Json4sUtil

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: derMicha
  * since: 2016-10-28
  */
class MessageProcessorActor extends Actor with ActorLogging {

  private implicit val exContext = context.dispatcher

  private val transformerActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[TransformerProducerActor]), ActorNames.TRANSFORMER_PRODUCER)

  private val persistenceActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[MessagePersistenceActor]), ActorNames.PERSISTENCE_SVC)

  private val notaryActor = context.actorOf(Props[MessageNotaryActor], ActorNames.NOTARY_SVC)

  val outboxManagerActor: ActorRef = context.actorOf(Props[DeviceOutboxManagerActor], ActorNames.DEVICE_OUTBOX_MANAGER)

  override def receive: Receive = {

    case (s: ActorRef, drd: DeviceDataRaw, device: Device) =>

      log.debug(s"received message: $drd")

      persistenceActor ! drd

      if (DeviceCoreUtil.checkNotaryUsage(device)) //TODO check notary config for device
        notaryActor ! drd

      Json4sUtil.any2jvalue(drd) match {
        case Some(drdJson) =>
          transformerActor ! Json4sUtil.jvalue2String(drdJson)
        case None =>
          log.error(s"could not create json for message: ${drd.id}")
      }

      //send back current device state
      val dbDevice = Json4sUtil.any2any[db.device.Device](device)
      val currentState = DeviceStateManager.currentDeviceState(dbDevice)
      DeviceStateManager.upsert(currentState)
      s ! currentState

      if (drd.uuid.isDefined) {
        val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(currentState).get)
        outboxManagerActor ! MessageReceiver(drd.uuid.get, currentStateStr, ConfigKeys.DEVICEOUTBOX)
      }

    case msg: CamelMessage =>
      //@TODO check why we receive here CamelMessages ???
      log.debug(s"received CamelMessage")

    case _ => log.error("received unknown message")

  }

}