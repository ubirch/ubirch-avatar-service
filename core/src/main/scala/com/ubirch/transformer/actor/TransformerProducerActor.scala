package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.Producer
import akka.kafka.ProducerSettings
import akka.pattern.pipe
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.kafka.KafkaProducer
import com.ubirch.transformer.actor.TransformerProducerActor2.{KafkaMessage, PublisherException, PublisherSuccess}
import com.ubirch.util.camel.CamelActorUtil
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Created by derMicha on 30/10/16.
  */
class TransformerProducerActor(queue: String)
  extends Actor
    with CamelActorUtil
    with Producer {

  val accessKey = Config.awsAccessKey

  val secretKey = Config.awsSecretAccessKey

  override def endpointUri: String = sqsEndpointConsumer(Config.sqsConfig(queue))

  //+ "&messageGroupIdStrategy=useExchangeId" +"&MessageDeduplicationIdStrategy=useExchangeId"

  override def oneway: Boolean = true

}


object TransformerProducerActor {
  def props(queue: String): Props = Props(new TransformerProducerActor(queue))
}

class TransformerProducerActor2(kafkaUrl: String, topicName: String) extends Actor with ActorLogging {
  implicit val ec: ExecutionContext = context.dispatcher

  // @TODO error handling
  private val kafkaProducer =
    try { new KafkaProducer(kafkaUrl, topicName, context.system) }
    catch { case ex: Exception => throw new RuntimeException(ex.getMessage) }

  override def receive: Receive = {
    case KafkaMessage(payload) =>
      log.info(s"publish payload to kafka")
      pipe {
        kafkaProducer.send(payload).map { recordMetadata =>
          log.info(s"success")
          PublisherSuccess(recordMetadata)
        }.recover {
          case e: Exception =>
            log.info(s"error")
            PublisherException(e.getCause)
        }
      } to self

    case PublisherSuccess(_) =>
      println("succeeded to send payload.")

    case PublisherException(err) =>
      println("succeeded to send payload.")
  }

  // is it necessary?
  override def postStop(): Unit = {
    log.debug("close kafka producer")
    kafkaProducer.close()
  }
}

object TransformerProducerActor2 {
  def props(kafkaUrl: String, topicName: String): Props = Props(classOf[TransformerProducerActor2], kafkaUrl, topicName)
  case class KafkaMessage(payload: String)
  sealed trait PublisherResult
  case class PublisherSuccess(recordMetadata: RecordMetadata) extends PublisherResult
  case class PublisherException(cause: Throwable) extends Exception("Kafka Publisher Exception", cause) with PublisherResult
}
