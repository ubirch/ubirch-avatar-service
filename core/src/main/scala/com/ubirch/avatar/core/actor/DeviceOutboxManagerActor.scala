package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.camel.CamelMessage
import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.model.actors.MessageReceiver
import com.ubirch.avatar.model.rest.device.DeviceDataRaw

import scala.collection.parallel.mutable

/**
  * Created by derMicha on 24/05/17.
  */

class DeviceOutboxManagerActor extends Actor with ActorLogging {

  val connections: mutable.ParHashMap[String, ActorRef] = mutable.ParHashMap()

  override def receive: Receive = {

    case drd: DeviceDataRaw =>


    case mr: MessageReceiver =>
      if (connections.keySet.contains(mr.getKey)) {
        log.debug(s"found MessageReceiver actorRef for: ${mr.getKey}")
        val actorRef = connections(mr.getKey)
        actorRef ! mr.message
      }
      else {
        log.debug(s"add new actorRef for: ${mr.getKey}")
        mr.target match {
          case ConfigKeys.DEVICEOUTBOX =>
            val deviceStateUpdateActor = context.actorOf(DeviceStateUpdateActor.props(mr.topic))
            connections.put(mr.getKey, deviceStateUpdateActor)
            deviceStateUpdateActor ! mr.message
          case _ =>
            log.error(s"invalid target: ${mr.target}")
        }
        log.debug(s"current producer counter: ${connections.size}")
      }
    case msg: CamelMessage =>
      //@TODO check why we receive here CamelMessages ???
      log.debug(s"received CamelMessage")
    case _ =>
      log.error("received unknown message")
  }
}
