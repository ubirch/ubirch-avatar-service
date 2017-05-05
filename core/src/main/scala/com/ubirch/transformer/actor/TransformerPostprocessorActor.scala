package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Kill}
import akka.camel.CamelMessage
import com.ubirch.avatar.core.actor.DeviceMessageProcessedActor
import com.ubirch.avatar.core.device.DeviceHistoryManager
import com.ubirch.avatar.model.device._
import com.ubirch.transformer.services.TransformerService
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerPostprocessorActor extends Actor with MyJsonProtocol with ActorLogging {

  implicit val executionContext = context.dispatcher

  override def receive: Receive = {

    case (deviceType: DeviceType, device: Device, drd: DeviceDataRaw) =>
      self forward(deviceType, device, drd, drd)
    case (deviceType: DeviceType, device: Device, drd: DeviceDataRaw, sdrd: DeviceDataRaw) =>
      log.debug(s"received device preprocessed raw data message: $drd with deviceKeyType: $deviceType")

      TransformerService.transform(
        deviceType = deviceType,
        device = device,
        drd = drd,
        sdrd = sdrd
      ) match {
        case Some(ddp) =>
          DeviceHistoryManager.store(ddp)

          Json4sUtil.any2jvalue(ddp) match {
            case Some(jval) =>

              log.debug("send processed message to sqs")
              if (device.pubQueues.isDefined) {
                device.pubQueues.get.foreach { sqsQueueName =>
                  log.debug(s"send processed message to $sqsQueueName")
                  val outProducerActor: ActorRef = context.actorOf(TransformerOutProducerActor.props(sqsQueueName))
                  outProducerActor ! Json4sUtil.jvalue2String(jval)
                  context.system.scheduler.scheduleOnce(15 seconds, outProducerActor, Kill)
                }
              }

              log.debug("send processed message to mqtt")
              val deviceMessageProcessedActor = context.actorOf(DeviceMessageProcessedActor.props(ddp.deviceId))
              deviceMessageProcessedActor ! Json4sUtil.jvalue2String(jval)

            case None =>
              log.error(s"could not parse to json: $ddp")
          }
        case None =>
          log.error("transformation failed")
      }
    case msg: CamelMessage =>
      log.debug(s"received CamelMessage")
    //@TODO check why we receive here CamelMessages ???
    case _ =>
      log.error(s"received unknown message from ${context.sender()} ")
  }
}