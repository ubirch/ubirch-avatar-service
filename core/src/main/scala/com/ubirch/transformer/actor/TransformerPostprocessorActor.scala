package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.core.device.DeviceDataProcessedManager
import com.ubirch.avatar.model.device._
import com.ubirch.util.json.MyJsonProtocol

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerPostprocessorActor extends Actor with MyJsonProtocol with ActorLogging {

  implicit val executionContext = context.dispatcher

  override def receive: Receive = {

    case (deviceType: DeviceType, device: Device, drd: DeviceDataRaw) =>
      self forward(deviceType, device, drd, drd)
    case (deviceType: DeviceType, device: Device, drd: DeviceDataRaw, sdrd: DeviceDataRaw) =>
      log.debug(s"received device raw data message: $drd with deviceKeyType: $deviceType")
      val ddp = DeviceDataProcessed(
        deviceId = device.deviceId,
        messageId = drd.id,
        deviceDataRawId = sdrd.id,
        deviceType = deviceType.key,
        timestamp = drd.ts,
        deviceTags = device.tags,
        deviceMessage = drd.p,
        deviceDataRaw = Some(sdrd)
      )
      DeviceDataProcessedManager.store(ddp)

    case _ =>
      log.error("received unknown message")
  }
}