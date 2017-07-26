package com.ubirch.transformer

import akka.actor.{ActorSystem, Props}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.actor.TransformerConsumerActor
import com.ubirch.util.mongo.connection.MongoUtil

/**
  * Created by derMicha on 30/10/16.
  */
object TransformerManager {

  implicit val system = ActorSystem()

  def init()(implicit mongo: MongoUtil): Unit = {

    system.actorOf(Props[TransformerConsumerActor], ActorNames.TRANSFORMER_CONSUMER)

  }
}
