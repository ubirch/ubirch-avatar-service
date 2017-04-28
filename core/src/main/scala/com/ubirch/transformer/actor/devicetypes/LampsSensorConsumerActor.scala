package com.ubirch.transformer.actor.devicetypes

import akka.actor.ActorLogging
import akka.camel.{CamelMessage, Consumer}
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 30/10/16.
  */
class LampsSensorConsumerActor extends Consumer with ActorLogging {

  val accessKey = System.getenv().get(Config.awsAccessKey)

  val secretKey = System.getenv().get(Config.awsSecretAccessKey)

  override def endpointUri = s"aws-sqs://${Config.awsSqsQueueTransformer}?accessKey=$accessKey&secretKey=$secretKey&delaySeconds=20"

  override def autoAck: Boolean = true

  implicit val executionContext = context.dispatcher

  override def receive = {
    case msg: CamelMessage =>
      //      msg.body
      log.debug(s"received ${msg.bodyAs[String]}")
    case _ =>
      log.error("received unknown message")
  }
}
