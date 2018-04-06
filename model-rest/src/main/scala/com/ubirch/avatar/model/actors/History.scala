package com.ubirch.avatar.model.actors

import java.util.UUID

import com.ubirch.avatar.model.rest.device.DeviceHistory
import org.joda.time.DateTime

case class HistoryByDate(deviceId: UUID, from: DateTime, to: DateTime)

case class HistoryBefore(deviceId: UUID, before: DateTime)

case class HistoryAfter(deviceId: UUID, after: DateTime)

case class HistoryByDay(deviceId: UUID, day: DateTime)

case class HistorySeq(seq: Seq[DeviceHistory])

