package com.ubirch.transformer.actor

import akka.camel.{CamelMessage, Producer}
import akka.actor.Actor

/**
  * Created by derMicha on 30/10/16.
  */
class TransformerProducerActor extends Actor with Producer {

  val accessKey = System.getenv().get("AWS_ACCESS_KEY_ID")

  val secretKey = System.getenv().get("AWS_SECRET_ACCESS_KEY")

  override def endpointUri = s"aws-sqs://sqs-akka-camel?accessKey=$accessKey&secretKey=$secretKey"

  override def oneway: Boolean = true

}
