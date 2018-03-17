package com.ubirch.avatar.backend.actor.util

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.backend.actor.DeviceApiActor
import com.ubirch.avatar.core.actor.{MessageProcessorActor, MessageValidatorActor, ReplayFilterActor}
import com.ubirch.avatar.core.udp.UDPReceiverActor
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.actor.TransformerConsumerActor
import com.ubirch.util.mongo.connection.MongoUtil

object ActorStarter extends StrictLogging {

  def init(system: ActorSystem)(implicit mongoUtil: MongoUtil, httpClient: HttpExt, materializer: Materializer) = {

    val d =
      system.actorOf(
        DeviceApiActor.props,
        ActorNames.DEVICE_API
      )

    val m =
      system.actorOf(
        MessageValidatorActor.props,
        ActorNames.MSG_VALIDATOR
      )

    val t =
      system.actorOf(
        TransformerConsumerActor.props,
        ActorNames.TRANSFORMER_CONSUMER)

    val ur =
      system.actorOf(
        Props(new UDPReceiverActor)
      )

    val p =
      system.actorOf(
        MessageProcessorActor.props(),
        ActorNames.MSG_PROCESSOR
      )

    val r =
      system.actorOf(
        ReplayFilterActor.props(),
        ActorNames.REPLAY_FILTER
      )


  }

}