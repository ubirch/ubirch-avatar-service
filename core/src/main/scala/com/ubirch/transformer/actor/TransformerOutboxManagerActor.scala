package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.camel.CamelMessage
import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.core.actor.DeviceMessageProcessedActor
import com.ubirch.transformer.model.MessageReceiver
import org.apache.camel.Message

import scala.collection.parallel.mutable

/**
  * Created by derMicha on 24/05/17.
  */

class TransformerOutboxManagerActor extends Actor with ActorLogging {

  val connections: mutable.ParHashMap[String, ActorRef] = mutable.ParHashMap()

  override def receive: Receive = {

    case mr: MessageReceiver =>
      if (connections.keySet.contains(mr.getKey)) {
        log.debug(s"found actorRef for: ${mr.getKey}")
        val actorRef = connections(mr.getKey)
        actorRef ! mr.message
      }
      else {
        log.debug(s"add new actorRef for: ${mr.getKey}")
        mr.target match {
          case ConfigKeys.INTERNOUTBOX =>
            val outProducerActor: ActorRef = context.actorOf(TransformerOutProducerActor.props(mr.topic))
            connections.put(mr.getKey, outProducerActor)
            outProducerActor ! mr.message
          case ConfigKeys.EXTERNOUTBOX =>
            val deviceMessageProcessedActor = context.actorOf(DeviceMessageProcessedActor.props(mr.topic))
            connections.put(mr.getKey, deviceMessageProcessedActor)
            deviceMessageProcessedActor ! mr.message
          case _ =>
            log.error(s"invalid target: ${mr.target}")
        }
        log.debug(s"current producer counter: ${connections.size}")
      }
  }

  override def unhandled(message: Any): Unit = {
    if (message.isInstanceOf[Message]) {
      //val m = message.asInstanceOf[Message]
      //log.error(s"received unknown message: ${m.getBody} from: ${context.sender()}")
    }
    else if (message.isInstanceOf[CamelMessage]) {
      //val m = message.asInstanceOf[CamelMessage]
      //log.error(s"received unknown message: ${m.toString()} from: ${context.sender()}")
    }
    else
      log.error(s"received unknown message: ${message.toString} (${message.getClass.toGenericString}) from: ${context.sender()}")
  }
}
