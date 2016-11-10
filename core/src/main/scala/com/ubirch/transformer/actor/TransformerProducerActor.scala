package com.ubirch.transformer.actor

import akka.actor.Actor
import akka.camel.Producer
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 30/10/16.
  */
class TransformerProducerActor extends Actor with Producer {

  val accessKey = System.getenv().get(Config.awsAccessKey)

  val secretKey = System.getenv().get(Config.awsSecretAccessKey)

  override def endpointUri = s"aws-sqs://${Config.awsSqsQueueTransformer}?accessKey=$accessKey&secretKey=$secretKey"

  override def oneway: Boolean = true

}
