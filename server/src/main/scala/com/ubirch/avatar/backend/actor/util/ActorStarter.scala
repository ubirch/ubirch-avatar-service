package com.ubirch.avatar.backend.actor.util

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.backend.actor.DeviceApiActor
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.MessageValidatorActor
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.mongo.connection.MongoUtil

object ActorStarter extends StrictLogging {

  def init(system: ActorSystem)(implicit mongoUtil: MongoUtil, httpClient: HttpExt, materializer: Materializer) = {

    val d = system.actorOf(
      new RoundRobinPool(
        Config.akkaNumberOfFrontendWorkers).props(Props(new DeviceApiActor())),
      ActorNames.DEVICE_API
    )

    val m = system.actorOf(
      new RoundRobinPool(
        Config.akkaNumberOfFrontendWorkers).props(Props(new MessageValidatorActor())),
      ActorNames.MSG_VALIDATOR
    )
  }
}