package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.core.device.{DeviceDataRawAnchoredManager, DeviceDataRawManager}
import com.ubirch.avatar.model.device.DeviceDataRaw

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

case class AnchoredRawData(raw: DeviceDataRaw)
