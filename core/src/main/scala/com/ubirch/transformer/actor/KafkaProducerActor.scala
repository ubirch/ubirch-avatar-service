package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.ubirch.avatar.core.kafka.KafkaProducer
import com.ubirch.transformer.actor.KafkaProducerActor.KafkaMessage

import scala.concurrent.ExecutionContext

class KafkaProducerActor(kafkaUrl: String, topic: String) extends Actor with ActorLogging {
  implicit val ec: ExecutionContext = context.dispatcher

  private val kafkaProducer =
    try { new KafkaProducer(kafkaUrl, topic, context.system) }
    catch { case ex: Exception =>
      log.error(s"Error occurred while initializing Kafka producer! ${ex.getMessage}")
      throw ex
    }

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
  def props(kafkaUrl: String, topic: String): Props = Props(new KafkaProducerActor(kafkaUrl, topic))
  case class KafkaMessage(payload: String)
}
