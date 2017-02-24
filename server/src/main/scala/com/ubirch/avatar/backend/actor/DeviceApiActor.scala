package com.ubirch.avatar.backend.actor

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.Device
import com.ubirch.util.model.JsonErrorResponse


/**
  * Created by derMicha on 30/10/16.
  */

case class CreateDevice(device: Device)

class DeviceApiActor extends Actor with StrictLogging {

  import context.dispatcher

  override def receive = {
    case cd: CreateDevice =>
      val from = sender
      DeviceManager.info(cd.device.deviceId).map {
        case Some(dev) =>
          from ! JsonErrorResponse(
            errorType = "CreationError",
            errorMessage = s"device already exist: $dev"
          )
        case None =>
          //          DeviceManager.createWithShadow(cd.device).map {
          DeviceManager.create(cd.device).map {
            case None =>
              from ! JsonErrorResponse(
                errorType = "CreationError",
                errorMessage = s"failed to create device: ${}"
              )
            case Some(deviceObject) =>
              from ! deviceObject
          }
      }
    case _ =>
      logger.error("received unknown message")
  }
}
