package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceDataRaw

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incomming messages
  */
class MessageECCValidatorActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case dm: DeviceDataRaw if (dm.v == Config.sdmV002) || (dm.v == Config.sdmV003) =>
      log.debug(s"received message with version ${dm.v}")

    case dm: DeviceDataRaw =>
      log.debug(s"received wrong message version: $dm")

    case _ =>
      log.error("received unknown message")
  }

}
