package com.ubirch.avatar.core.kafka

import akka.actor.ActorSystem
import akka.kafka.{ProducerSettings}
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

class KafkaProducer(kafkaUrl: String, topic: String, actorSystem: ActorSystem) {

  val producerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers(kafkaUrl)

  val producer = producerSettings.createKafkaProducer()

  def send(payload: String)(implicit ec: ExecutionContext): Future[RecordMetadata] = {
    val record = new ProducerRecord[String, String](topic, payload)
    val promise = Promise[RecordMetadata]()
    try {
      producer.send(record)
    } catch {
      case NonFatal(e) => promise.failure(e)
    }
    promise.future
  }

  def close(): Unit = {
    producer.close()
  }
}
