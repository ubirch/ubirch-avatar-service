package com.ubirch.transformer.actor

import akka.actor.Actor
import akka.camel.Producer
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 30/10/16.
  */
class TransformerOutProducerActor extends Actor with Producer {

  val accessKey = Config.awsAccessKey

  val secretKey = Config.awsSecretAccessKey

  override def endpointUri = s"aws-sqs://${Config.awsSqsQueueTransformerOut}?accessKey=$accessKey&secretKey=$secretKey&delaySeconds=30"

  override def oneway: Boolean = true

}
