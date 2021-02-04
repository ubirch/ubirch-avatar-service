package com.ubirch.avatar.backend.actor.util

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.backend.actor.DeviceApiActor
import com.ubirch.avatar.core.actor._
import com.ubirch.avatar.core.udp.UDPReceiverActor
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.actor.{KafkaProducerActor, TransformerConsumerActor}
import com.ubirch.util.mongo.connection.MongoUtil

object ActorStarter extends StrictLogging {

  def init(system: ActorSystem)(implicit mongoUtil: MongoUtil, httpClient: HttpExt, materializer: Materializer): Unit = {

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

    val mp =
      system.actorOf(
        MessageMsgPackProcessorActor.props(),
        ActorNames.MSG_MSGPACK_PROCESSOR
      )

    val ob = system.actorOf(
      DeviceOutboxManagerActor.props(),
      ActorNames.DEVICE_OUTBOX_MANAGER
    )

    // @todo config
    val ts = system.actorOf(
      KafkaProducerActor.props("localhost:9092", "trackle-msgpack"),
      ActorNames.kafkaProducer("trackle-msgpack")
    )

    //    val din = system.actorOf(
    //      MqttDeviceConsumerActor.props,
    //      ActorNames.DEVICE_INBOX
    //    )
  }

}