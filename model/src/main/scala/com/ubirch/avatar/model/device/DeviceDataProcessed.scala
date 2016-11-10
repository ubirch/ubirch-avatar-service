package com.ubirch.avatar.model.device

import java.util.UUID

import org.joda.time.DateTime
import org.json4s._

/**
  * author: cvandrei
  * since: 2016-11-02
  */
case class DeviceDataProcessed(deviceId: String,
                               messageId: UUID,
                               deviceType: String,
                               timestamp: DateTime,
                               deviceTags: Set[String],
                               deviceMessage: JValue
                              )
