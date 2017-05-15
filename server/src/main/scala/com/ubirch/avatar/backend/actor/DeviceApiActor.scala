package com.ubirch.avatar.backend.actor

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.avatar.model._
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.uuid.UUIDUtil

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
          val dbDevice = addGroup(cd.session, cd.device)
          logger.debug(s"creating: db.device.Device=$dbDevice")
          DeviceManager.create(dbDevice).map {
            case None =>
              from ! JsonErrorResponse(
                errorType = "CreationError",
                errorMessage = s"failed to create device: ${cd.device.deviceId}"
              )
            case Some(deviceObject) =>
              from ! deviceObject
          }

      }

    case _ => logger.error("received unknown message")

  }

  private def addGroup(session: AvatarSession, device: Device) = {

    val groups = Set(UUIDUtil.uuid, UUIDUtil.uuid) // TODO ask user-service for groups
    Json4sUtil.any2any[db.device.Device](device).copy(groups = groups)

  }

}
