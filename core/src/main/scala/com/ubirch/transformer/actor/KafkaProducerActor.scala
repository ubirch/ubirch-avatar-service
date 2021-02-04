package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.ubirch.avatar.core.kafka.KafkaProducer
import com.ubirch.transformer.actor.KafkaProducerActor.KafkaMessage

import scala.concurrent.ExecutionContext

class KafkaProducerActor(kafkaUrl: String, topicName: String) extends Actor with ActorLogging {
  implicit val ec: ExecutionContext = context.dispatcher

  // @TODO error handling
  private val kafkaProducer =
    try { new KafkaProducer(kafkaUrl, topicName, context.system) }
    catch { case ex: Exception => throw new RuntimeException(ex.getMessage) }

  override def receive: Receive = {
    case KafkaMessage(payload) =>
      log.info(s"publish payload to kafka")
      kafkaProducer.send(payload).pipeTo(sender())
  }

  override def postStop(): Unit = {
    log.debug("close kafka producer")
    kafkaProducer.close()
  }
}

object KafkaProducerActor {
  def props(kafkaUrl: String, topicName: String): Props = Props(classOf[KafkaProducerActor], kafkaUrl, topicName)
  case class KafkaMessage(payload: String)
}
