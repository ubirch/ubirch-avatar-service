package com.ubirch.avatar.model.db.device

import java.util.UUID

import org.joda.time.{ DateTime, DateTimeZone }
import org.json4s.JValue

/**
  * author: cvandrei
  * since: 2017-05-12
  */
case class Device(
  deviceId: String,
  owners: Set[UUID] = Set.empty,
  groups: Set[UUID] = Set.empty,
  deviceTypeKey: String = "unknownDeviceType",
  deviceName: String = "unnamedDevice",
  hwDeviceId: String = "unknownHwDeviceId",
  hashedHwDeviceId: String = "unknownHwDeviceId",
  tags: Set[String] = Set(),
  deviceConfig: Option[JValue] = None,
  deviceProperties: Option[JValue] = None,
  subQueues: Option[Set[String]] = None,
  pubQueues: Option[Set[String]] = None,
  pubRawQueues: Option[Set[String]] = None,
  avatarLastUpdated: Option[DateTime] = None,
  deviceLastUpdated: Option[DateTime] = None,
  updated: Option[DateTime] = None,
  created: DateTime = DateTime.now(DateTimeZone.UTC)) {

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case dev: Device => dev.deviceId == this.deviceId
      case _           => false
    }
  }

  override def hashCode(): Int = this.deviceId.hashCode
}
