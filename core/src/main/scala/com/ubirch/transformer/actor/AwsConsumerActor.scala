package com.ubirch.transformer.actor

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.camel.{CamelMessage, Consumer}
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 30/10/16.
  */
class AwsConsumerActor extends Consumer with ActorLogging {

  val accessKey = System.getenv().get(Config.awsAccessKey)

  val secretKey = System.getenv().get(Config.awsSecretAccessKey)

  override def endpointUri = s"aws-sqs://${Config.awsSqsQueueTransformer}?accessKey=$accessKey&secretKey=$secretKey"

  override def autoAck: Boolean = true

  val transformerActor = context.actorOf(Props[TransformerActor], "transformer-actor")

  override def receive = {
    case msg: CamelMessage =>
      msg.body match {
        case idStr: String =>
          try {
            val id = UUID.fromString(idStr)
            log.debug(s"received id: $id")
            //            DeviceDataRawManager.
          } catch {
            case e: Exception =>
              log.error(s"received invalid id: $idStr", e)
          }
        case _ =>
          log.error(s"received invalid message body: ${msg.body}")
      }
      log.debug(s"received ${msg.bodyAs[String]}")
    case _ =>
      log.error("received unknown message")
  }
}
