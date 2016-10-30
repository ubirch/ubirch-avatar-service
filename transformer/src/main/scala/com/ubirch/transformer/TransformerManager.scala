package com.ubirch.transformer

import akka.actor.{ActorSystem, Props}
import com.ubirch.transformer.actor.AwsConsumerActor

/**
  * Created by derMicha on 30/10/16.
  */
object TransformerManager {

  implicit val system = ActorSystem()

  def init(): Unit = {

    system.actorOf(Props[AwsConsumerActor], "transformer-consumer")

  }
}
