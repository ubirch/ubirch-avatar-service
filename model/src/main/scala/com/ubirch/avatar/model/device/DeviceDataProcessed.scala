package com.ubirch.avatar.model.device

import java.util.UUID

import org.joda.time.DateTime
import org.json4s._

/**
  * author: cvandrei
  * since: 2016-11-02
  */

case class DeviceDataProcessed(messageId: UUID,
                               deviceDataRawId: UUID,
                               deviceId: String,
                               deviceName: String,
                               deviceType: String,
                               deviceTags: Set[String],
                               deviceMessage: JValue,
                               deviceDataRaw: Option[DeviceDataRaw] = None,
                               timestamp: DateTime
                              )

case class DeviceDataProcessedLegacy(messageId: UUID,
                                     deviceDataRawId: UUID,
                                     deviceId: String,
                                     deviceName: Option[String] = None,
                                     deviceType: String,
                                     deviceTags: Set[String],
                                     deviceMessage: JValue,
                                     deviceDataRaw: Option[DeviceDataRaw] = None,
                                     timestamp: DateTime
                                    )


//
//object DeviceDataProcessed {
//
//  def apply(messageId: UUID,
//            deviceDataRawId: UUID,
//            deviceId: String,
//            deviceName: String = "",
//            deviceType: String,
//            deviceTags: Set[String],
//            deviceMessage: JValue,
//            deviceDataRaw: Option[DeviceDataRaw] = None,
//            timestamp: DateTime
//           ) = {
//
//    val dn = if (deviceName.isEmpty)
//      s"$deviceType-$deviceId"
//    else
//      deviceName.trim
//
//    new DeviceDataProcessed(messageId = messageId,
//      deviceDataRawId = deviceDataRawId,
//      deviceId = deviceId,
//      deviceName = dn,
//      deviceType = deviceType,
//      deviceTags = deviceTags,
//      deviceMessage = deviceMessage,
//      deviceDataRaw = deviceDataRaw,
//      timestamp = timestamp
//    ) {}
//  }
//}
