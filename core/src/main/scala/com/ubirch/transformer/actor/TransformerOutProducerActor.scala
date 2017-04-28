package com.ubirch.transformer.actor

import akka.actor.{Actor, Props}
import akka.camel.Producer
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 30/10/16.
  */
class TransformerOutProducerActor(sqsQueueName: String) extends Actor with Producer {

  val accessKey = Config.awsAccessKey

  val secretKey = Config.awsSecretAccessKey

  override def endpointUri = s"aws-sqs://${sqsQueueName}?accessKey=$accessKey&secretKey=$secretKey&delaySeconds=20"

  override def oneway: Boolean = true

}

object TransformerOutProducerActor {
  def props(sqsQueueName: String): Props = Props(new TransformerOutProducerActor(sqsQueueName))
}