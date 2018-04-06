package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.camel.CamelMessage
import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.model.actors.MessageReceiver
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.transformer.actor.TransformerProducerActor
import com.ubirch.util.json.Json4sUtil
import org.apache.camel.Message

import scala.collection.parallel.mutable

/**
  * Created by derMicha on 24/05/17.
  */

class DeviceOutboxManagerActor extends Actor with ActorLogging {

  val connections: mutable.ParHashMap[String, ActorRef] = mutable.ParHashMap()

  override def receive: Receive = {

    case (device: Device, drd: DeviceDataRaw) =>
      val drdExt = drd.copy(deviceId = Some(device.deviceId))
      device.pubRawQueues.getOrElse(Set()).foreach { queue =>
        val taRef = if (connections.keySet.contains(queue)) {
          log.debug(s"found MessageReceiver actorRef for: $queue")
          val transformerActor = connections(queue)
          transformerActor
        }
        else {
          log.debug(s"add new actorRef for: $queue")
          val transformerActor: ActorRef = context
            .actorOf(TransformerProducerActor.props(queue))
          connections.put(queue, transformerActor)
          transformerActor
        }
        Json4sUtil.any2String(drdExt) match {
          case Some(drdStr) =>
            taRef ! drdStr
          case None =>
            log.error(s"error sending for device ${device.deviceId} raw message ${drd.id}")
        }
      }

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
  }

  override def unhandled(message: Any): Unit = {
    if (message.isInstanceOf[Message]) {
      //log.error(s"received unknown message body: ${message.asInstanceOf[Message].getBody.toString}")
    }
    else if (message.isInstanceOf[CamelMessage]) {
      //log.error(s"received unknown message body: ${message.asInstanceOf[Message].getBody.toString}")
    }
    else
      log.error(s"${message.getClass.toString}")
  }
}

object DeviceOutboxManagerActor {
  //TODO add Router here !!
  def props(): Props = Props(new DeviceOutboxManagerActor())
}

