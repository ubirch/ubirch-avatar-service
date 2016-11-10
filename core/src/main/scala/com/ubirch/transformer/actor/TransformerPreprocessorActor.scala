package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw, DeviceDataRawEnvelope, DeviceType}
import com.ubirch.services.util.DeviceUtil
import com.ubirch.util.json.MyJsonProtocol
import org.json4s.JValue

/**
  * Created by derMicha on 28/10/16.
  */
class TransformerPreprocessorActor extends Actor with MyJsonProtocol with ActorLogging {

  implicit val executionContext = context.dispatcher

  val transformPostActor = context.actorOf(Props[TransformerPostprocessorActor], "transformer-post-actor")

  override def receive: Receive = {

    case (device: Device, dme: DeviceDataRawEnvelope) =>
      log.debug(s"received device raw data  message: $dme from device: $device")

    case (device: Device, drd: DeviceDataRaw) =>
      log.debug(s"received device raw data message: $drd from device: $device")
      DeviceTypeManager.getByKey(device.deviceTypeKey).map { currentDeviceType =>

        val dt = currentDeviceType.getOrElse(DeviceUtil.defaultDeviceType())

        drd.v match {
          case Config.sdmV001 =>
            transformPostActor ! (dt, device, drd)

          case Config.sdmV002 =>
            transformPostActor ! (dt, device, drd)

          case Config.sdmV003 =>
            drd.p.extract[Array[JValue]].foreach { payload =>
              log.debug(s"extracted payload: $payload")
              val newDrd = drd.copy(
                v = Config.sdmV002,
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
