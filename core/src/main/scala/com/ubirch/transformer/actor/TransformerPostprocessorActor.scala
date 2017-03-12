package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.camel.CamelMessage
import com.ubirch.avatar.core.actor.DeviceMessageProcessedActor
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

          log.debug("send processed message to sqs")
          outProducerActor ! Json4sUtil.jvalue2String(jval)

          log.debug("send processed message to mqtt")
          val deviceMessageProcessedActor = context.actorOf(DeviceMessageProcessedActor.props(ddp.deviceId))
          deviceMessageProcessedActor ! Json4sUtil.jvalue2String(jval)

        case None =>
          log.error(s"could not parse to json: $ddp")
      }

    case msg: CamelMessage =>
      log.debug(s"received CamelMessage")
    //@TODO check why we receive here CamelMessages ???
    case _ =>
      log.error(s"received unknown message from ${context.sender()} ")
  }
}