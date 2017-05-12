package com.ubirch.avatar.backend.actor

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.util.model.JsonErrorResponse

import akka.actor.Actor

/**
  * Created by derMicha on 30/10/16.
  */

case class CreateDevice(session: AvatarSession, device: Device)

class DeviceApiActor extends Actor with StrictLogging {

  import context.dispatcher

  override def receive: Receive = {

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
          // TODO set device.groups and transform to db.Device
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

    case _ => logger.error("received unknown message")

  }

}
