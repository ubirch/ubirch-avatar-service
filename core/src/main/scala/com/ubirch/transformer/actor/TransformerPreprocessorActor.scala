package com.ubirch.transformer.actor

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.{Device, DeviceDataRaw, DeviceDataRawEnvelope}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.util.json.MyJsonProtocol

import org.json4s.JValue

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerPreprocessorActor extends Actor with MyJsonProtocol with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  val transformPostActor: ActorRef = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[TransformerPostprocessorActor]), ActorNames.TRANSFORMER_POST)

  override def receive: Receive = {

    case (device: Device, dme: DeviceDataRawEnvelope) =>
      log.debug(s"received device raw data  message: $dme from device: $device")

    case (device: Device, drd: DeviceDataRaw) =>
      log.debug(s"received device raw data message: $drd from device: $device")
      DeviceTypeManager.getByKey(device.deviceTypeKey).map { currentDeviceType =>

        val dt = currentDeviceType.getOrElse(DeviceTypeUtil.defaultDeviceType())

        log.debug(s"v: ${drd.v} / dt: $dt / device: $device / drd: $drd")

        drd.v match {

          case MessageVersion.`v000` =>

            transformPostActor ! (dt, device, drd)

          case MessageVersion.`v001` =>
            transformPostActor ! (dt, device, drd)

          case MessageVersion.`v002` =>
            transformPostActor ! (dt, device, drd)

          case MessageVersion.`v003` =>
            drd.p.extract[Array[JValue]].foreach { payload =>
              log.debug(s"extracted payload: $payload")
              val newDrd = drd.copy(

                v = MessageVersion.v002,
                p = payload
              )
              transformPostActor ! (dt, device, newDrd, drd)
            }
        }
      }
    case _ =>
      log.error("received unknown message")
  }
}
