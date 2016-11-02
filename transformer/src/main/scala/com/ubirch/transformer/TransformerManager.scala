package com.ubirch.transformer

import akka.actor.{ActorSystem, Props}
import com.ubirch.transformer.actor.AwsConsumerActor

/**
  * Created by derMicha on 30/10/16.
  */
object TransformerManager {

  implicit val system = ActorSystem()

  def init(): Unit = {


    //TODO we have to find a way to register our own awsSqsClient !!
    //-> http://camel.apache.org/aws-sqs.html -> Advanced AmazonSQS configuration
    //    val ctx = new DefaultCamelContext()
    //    ctx.addComponent()
    //    ctx.bind("client", client);
    system.actorOf(Props[AwsConsumerActor], "transformer-consumer")

  }
}
