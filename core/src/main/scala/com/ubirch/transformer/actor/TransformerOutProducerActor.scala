package com.ubirch.transformer.actor

import akka.actor.{Actor, Props}
import akka.camel.Producer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.util.CamelActorUtil

/**
  * Created by derMicha on 30/10/16.
  */
class TransformerOutProducerActor(sqsQueueName: String)
  extends Actor
    with CamelActorUtil
    with Producer {

  val accessKey = Config.awsAccessKey

  val secretKey = Config.awsSecretAccessKey

  override def endpointUri = sqsEndpointConsumer(sqsQueueName)

  override def oneway: Boolean = true
}

object TransformerOutProducerActor extends StrictLogging {

  def props(sqsQueueName: String): Props = {
    logger.debug(s"TransformerOutProducerActor with queue: $sqsQueueName")
    Props(new TransformerOutProducerActor(sqsQueueName))
  }

}