package com.ubirch.avatar.model.rest.device

import java.util.UUID

import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import org.json4s.JValue

case class MsgPackMessage(
                           messageId: UUID = UUIDUtil.uuid,
                           messageType: Int,
                           deviceId: String,
                           payload: JValue,
                           created: DateTime = DateTime.now,
                           signature: Option[String] = None
                         )