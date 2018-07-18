package com.ubirch.avatar.core.actor

import akka.actor.{Actor, Props}
import akka.camel.Producer
import com.ubirch.avatar.config.Config
import com.ubirch.util.camel.CamelActorUtil

class MessageChainProducerActor(sqsQueueName: String)
  extends Actor
    with CamelActorUtil
    with Producer {

  val accessKey = Config.awsAccessKey

  val secretKey = Config.awsSecretAccessKey

  override def endpointUri: String = sqsEndpointConsumer(Config.sqsConfig(sqsQueueName))

  //+ "&messageGroupIdStrategy=useExchangeId&MessageDeduplicationIdStrategy=useExchangeId"

  override def oneway: Boolean = true
}

object MessageChainProducerActor {
  def props(sqsQueueName: String): Props = Props(new MessageChainProducerActor(sqsQueueName))
}

