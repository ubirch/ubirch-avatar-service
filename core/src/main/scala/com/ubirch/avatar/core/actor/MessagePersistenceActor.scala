package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.{DeviceDataRawAnchoredManager, DeviceDataRawManager}
import com.ubirch.avatar.model.actors.AnchoredRawData
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames

/**
  * Created by derMicha on 28/10/16.
  */
class MessagePersistenceActor extends Actor with ActorLogging {

  override def receive: Receive = {

    case drd: DeviceDataRaw =>
      log.debug(s"received message: $drd")
      DeviceDataRawManager.store(drd.copy(txHash = None))

    case anchored: AnchoredRawData =>
      log.debug(s"received message (anchored raw data): $anchored")
      DeviceDataRawAnchoredManager.store(anchored.raw)

    case _ => log.error("received unknown message")

  }

}

object MessagePersistenceActor {
  def props: Props = new RoundRobinPool(Config.akkaNumberOfBackendWorkers).props(Props[MessagePersistenceActor])
}
