package com.ubirch.avatar.core.kafka

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.{Callback, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

class KafkaProducer(kafkaUrl: String, topic: String, actorSystem: ActorSystem) {

  val producerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers(kafkaUrl)

  val producer = producerSettings.createKafkaProducer()

  def send(message: String)(implicit ec: ExecutionContext): Future[RecordMetadata] = {
    val record = new ProducerRecord[String, String](topic, message)
    val promise = Promise[RecordMetadata]()
    try {
      producer.send(record, producerCallback(result => promise.complete(result)))
    } catch {
      case NonFatal(e) => promise.failure(e)
    }
    promise.future
  }

  def close(): Unit = {
    producer.close()
  }

  private def producerCallback(callback: Try[RecordMetadata] => Unit): Callback =
    new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        val result = {
          if(exception == null) Success(metadata)
          else Failure(exception)
        }
        callback(result)
      }
    }
}

object KafkaProducer extends StrictLogging {
  /**
   * throw exception when the initialization is failed
   */
  def create(kafkaUrl: String, topic: String, actorSystem: ActorSystem): KafkaProducer =
    try { new KafkaProducer(kafkaUrl, topic, actorSystem) }
    catch { case ex: Exception =>
      logger.error(s"Error occurred while initializing Kafka producer! ${ex.getMessage}")
      throw ex
    }

}
