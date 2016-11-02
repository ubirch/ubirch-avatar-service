package com.ubirch.avatar.model.device

import org.joda.time.DateTime
import org.json4s._

/**
  * author: cvandrei
  * since: 2016-11-02
  */
case class DeviceDataProcessed(deviceId: String,
                               messageId: String,
                               deviceType: String,
                               timestamp: DateTime,
                               deviceTags: Seq[String],
                               deviceMessage: JValue
                              )
