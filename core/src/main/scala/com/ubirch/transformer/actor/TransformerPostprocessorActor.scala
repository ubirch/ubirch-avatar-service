package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceHistoryManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceType}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.model.MessageReceiver
import com.ubirch.transformer.services.TransformerService
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerPostprocessorActor extends Actor with MyJsonProtocol with ActorLogging {

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher

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
          log.debug(s"try to store data for device: ${ddp.deviceId} after transforming")
          DeviceHistoryManager.store(ddp)
          log.debug(s"stored data for device: ${ddp.deviceId} after transforming")

          Json4sUtil.any2jvalue(ddp) match {
            case Some(jval) =>

              log.debug("send processed message to sqs")

              val jvalStr = Json4sUtil.jvalue2String(jval)

              if (device.pubQueues.isDefined) {
                device.pubQueues.get.foreach { sqsQueueName =>
                  log.debug(s"send processed message for deviceId ${device.deviceId} to $sqsQueueName")
                  outboxManagerActor ! MessageReceiver(sqsQueueName, jvalStr, ConfigKeys.INTERNOUTBOX)
                }
              }
              else
                log.info(s"no pubQueues defined for deviceId: ${device.deviceId}")

              if (Config.mqttPublishProcessed) {
                log.debug(s"currently do not send processed message to mqtt for deviceId ${device.deviceId}")
                outboxManagerActor ! MessageReceiver(ddp.deviceId, jvalStr, ConfigKeys.EXTERNOUTBOX)
              }
              else
                log.info(s"do not send processed message for deviceId ${device.deviceId} to mqtt")

            case None =>
              log.error(s"could not parse to json: $ddp")
          }
        case None =>
          log.error("transformation failed")
      }
  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received unknown message: ${message.toString} from: ${context.sender()}")
  }
}

object TransformerPostprocessorActor {
  def props: Props = new RoundRobinPool(Config.akkaNumberOfBackendWorkers)
    .props(Props[TransformerPostprocessorActor])
}