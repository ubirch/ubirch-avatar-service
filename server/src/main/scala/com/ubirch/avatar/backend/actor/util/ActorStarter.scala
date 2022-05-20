package com.ubirch.avatar.backend.actor.util

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.backend.actor.DeviceApiActor
import com.ubirch.avatar.core.actor._
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.mongo.connection.MongoUtil

object ActorStarter extends StrictLogging {

  def init(system: ActorSystem)(
    implicit mongoUtil: MongoUtil,
    httpClient: HttpExt,
    materializer: Materializer): Unit = {

    system.actorOf(
      DeviceApiActor.props(),
      ActorNames.DEVICE_API
    )

    system.actorOf(
      MessageValidatorActor.props(),
      ActorNames.MSG_VALIDATOR
    )

    system.actorOf(
      MessageProcessorActor.props(),
      ActorNames.MSG_PROCESSOR
    )

    system.actorOf(
      MessageMsgPackProcessorActor.props(),
      ActorNames.MSG_MSGPACK_PROCESSOR
    )

    system.actorOf(
      DeviceOutboxManagerActor.props(),
      ActorNames.DEVICE_OUTBOX_MANAGER
    )
  }

}
