package com.ubirch.avatar.model.device

import org.json4s.JValue

/**
  * * Created by derMicha on 28/10/16.
  *
  * @param deviceTypeKey     unique desricptive id which indentifies the current DeviceType
  * @param deviceTypeName    human understandable name
  * @param defaultProperties default properties of a device
  * @param defaultTags       default tags of a device
  */
case class DeviceType(
                       deviceTypeKey: String,
                       deviceTypeName: String,
                       defaultProperties: JValue,
                       defaultTags: JValue
                     )

