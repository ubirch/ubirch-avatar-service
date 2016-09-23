package com.ubirch.avatar.model

import org.joda.time.DateTime
import org.json4s.JValue

/**
  * author: cvandrei
  * since: 2016-09-23
  */
// TODO which fields are be optional?
case class Device(deviceId: String,
                  hwDeviceId: String,
                  deviceName: String,
                  deviceType: String,
                  deviceConfig: JValue,
                  tags: Set[String],
                  deviceProperties: JValue,
                  subscriptions: Set[String],
                  created: DateTime,
                  updated: DateTime)
