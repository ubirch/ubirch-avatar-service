package com.ubirch.avatar.model.device

import java.util.UUID

import org.json4s.JValue

/**
  * * Created by derMicha on 28/10/16.
  *
  * @param deviceTypeId      unique id which indentifies the current DeviceType
  * @param deviceTypName     human understandable name
  * @param defaultProperties default properties of a device
  * @param defaultTags       default tags of a device
  */
case class DeviceType(deviceTypeId: UUID, deviceTypName: String, deviceTypKey: String, defaultProperties: JValue, defaultTags: JValue)

