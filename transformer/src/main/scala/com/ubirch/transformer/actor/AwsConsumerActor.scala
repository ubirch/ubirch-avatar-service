package com.ubirch.transformer.actor

import akka.actor.ActorLogging
import akka.camel.{CamelMessage, Consumer}

/**
  * Created by derMicha on 30/10/16.
  */
class AwsConsumerActor extends Consumer with ActorLogging {

  val accessKey = System.getenv().get("AWS_ACCESS_KEY_ID")

  val secretKey = System.getenv().get("AWS_SECRET_ACCESS_KEY")

  override def endpointUri = s"aws-sqs://sqs-akka-camel?accessKey=$accessKey&secretKey=$secretKey"

  override def autoAck: Boolean = true

  override def receive = {
    case msg: CamelMessage =>

      log.debug(s"received ${msg.bodyAs[String]}")
    case _ =>
      log.error("received unknown message")
  }
}
