package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ubirch.avatar.core.device.DeviceDataProcessedManager
import com.ubirch.avatar.model.device._
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.services.TransformerService
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerPostprocessorActor extends Actor with MyJsonProtocol with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  val outProducerActor: ActorRef = context.actorOf(Props[TransformerOutProducerActor], ActorNames.OUT_PRODUCER)

  override def receive: Receive = {

    case (deviceType: DeviceType, device: Device, drd: DeviceDataRaw) =>
      self forward(deviceType, device, drd, drd)
    case (deviceType: DeviceType, device: Device, drd: DeviceDataRaw, sdrd: DeviceDataRaw) =>
      log.debug(s"received device preprocessed raw data message: $drd with deviceKeyType: $deviceType")

      val ddp = TransformerService.transform(
        deviceType = deviceType,
        device = device,
        drd = drd,
        sdrd = sdrd
      )

      DeviceDataProcessedManager.store(ddp)

      val jval = Json4sUtil.any2jvalue(ddp) match {
        case Some(jval) =>
          outProducerActor ! Json4sUtil.jvalue2String(jval)
        case None =>
          log.error(s"could not parse to json: $ddp")
      }


    case _ =>
      log.error("received unknown message")
  }
}