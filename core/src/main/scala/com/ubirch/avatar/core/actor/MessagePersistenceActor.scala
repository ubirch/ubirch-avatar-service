package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.{DeviceDataRawAnchoredManager, DeviceDataRawManager}
import com.ubirch.avatar.model.actors.AnchoredRawData
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceStateUpdate}

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 28/10/16.
  */
class MessagePersistenceActor extends Actor with ActorLogging {
  private implicit val exContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {

    case (drd: DeviceDataRaw, d: DeviceStateUpdate) =>
      val s = context.sender()
      log.debug(s"received message: $drd")
      DeviceDataRawManager.store(drd.copy(txHash = None)).map {
        case Some(ddr) =>
          log.debug(s"successfully stored deviceDataRaw for device with id ${ddr.deviceId} in database.")
          s ! d
        case None =>
          log.error("something went wrong storing the deviceDataRaw in database.")
      }

    case anchored: AnchoredRawData =>
      log.debug(s"received message (anchored raw data): $anchored")
      DeviceDataRawAnchoredManager.store(anchored.raw)

    case _ => log.error("received unknown message")

  }

}

object MessagePersistenceActor {
  def props: Props = new RoundRobinPool(Config.akkaNumberOfBackendWorkers).props(Props[MessagePersistenceActor])
}
