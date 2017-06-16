package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceHistoryManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceType}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.model.MessageReceiver
import com.ubirch.transformer.services.TransformerService
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import scala.language.postfixOps

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerPostprocessorActor extends Actor with MyJsonProtocol with ActorLogging {

  private implicit val executionContext = context.dispatcher

  val outboxManagerActor: ActorRef = context.actorOf(Props[TransformerOutboxManagerActor], ActorNames.TRANSFORMER_OUTBOX_MANAGER)

  override def receive: Receive = {

    case (deviceType: DeviceType, device: Device, drd: DeviceDataRaw) =>
      self forward(deviceType, device, drd, drd)
    case (deviceType: DeviceType, device: Device, drd: DeviceDataRaw, sdrd: DeviceDataRaw) =>
      log.debug(s"received device preprocessed raw data message: $drd with deviceKeyType: $deviceType")

      val dbDevice = Json4sUtil.any2any[db.device.Device](device)
      TransformerService.transform(
        deviceType = deviceType,
        device = dbDevice,
        drd = drd,
        sdrd = sdrd
      ) match {
        case Some(ddp) =>
          DeviceHistoryManager.store(ddp)

          Json4sUtil.any2jvalue(ddp) match {
            case Some(jval) =>

              log.debug("send processed message to sqs")

              val jvalStr = Json4sUtil.jvalue2String(jval)

              if (device.pubQueues.isDefined) {
                device.pubQueues.get.foreach { sqsQueueName =>
                  log.debug(s"send processed message to $sqsQueueName")
                  outboxManagerActor ! MessageReceiver(sqsQueueName, jvalStr, ConfigKeys.INTERNOUTBOX)
                }
              }

              if (Config.mqttPublishProcessed) {
                log.debug("send processed message to mqtt")
                outboxManagerActor ! MessageReceiver(ddp.deviceId, jvalStr, ConfigKeys.EXTERNOUTBOX)
              }
              else
                log.info("do not send processed message to mqtt")


            case None =>
              log.error(s"could not parse to json: $ddp")
          }
        case None =>
          log.error("transformation failed")
      }
    case _ =>
      log.error(s"received unknown message from ${context.sender()} ")
  }
}