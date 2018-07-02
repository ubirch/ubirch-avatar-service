package com.ubirch.core.protocol.msgpack

import java.util.UUID

import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import org.json4s.JValue

case class MsgPackMessage(
                           messageId: UUID = UUIDUtil.uuid,
                           messageType: Int,
                           deviceId: String,
                           payloadJson: JValue,
                           payloadBin: Option[Array[Byte]] = None,
                           created: DateTime = DateTime.now,
                           signature: Option[String] = None
                         )

case class MsgPackMessageV2(
                             messageId: UUID = UUIDUtil.uuid,
                             messageVersion: String,
                             firmwareVersion: String,
                             hwDeviceId: String,
                             prevMessageHash: Option[String] = None,
                             payloadJson: JValue,
                             payloadBin: Array[Byte],
                             errorCode: Int,
                             created: DateTime = DateTime.now,
                             signature: Option[String] = None
                           )