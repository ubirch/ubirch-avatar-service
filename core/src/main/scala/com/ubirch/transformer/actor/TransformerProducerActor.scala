package com.ubirch.transformer.actor

import akka.actor.Actor
import akka.camel.Producer
import com.ubirch.avatar.config.Config
import com.ubirch.util.camel.CamelActorUtil

/**
  * Created by derMicha on 30/10/16.
  */
class TransformerProducerActor
  extends Actor
    with CamelActorUtil
    with Producer {

  val accessKey = Config.awsAccessKey

  val secretKey = Config.awsSecretAccessKey

  override def endpointUri: String = sqsEndpointConsumer(Config.sqsConfig(Config.awsSqsQueueTransformer))

  override def oneway: Boolean = true

}
