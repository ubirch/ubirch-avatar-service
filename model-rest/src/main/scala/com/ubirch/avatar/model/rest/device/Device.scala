package com.ubirch.avatar.model.rest.device

import java.util.UUID

import com.ubirch.avatar.config.Config

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JValue

/**
  * author: cvandrei
  * since: 2016-09-23
  */
case class Device(deviceId: String,
                  groups: Set[UUID] = Set.empty,
                  deviceTypeKey: String = "unknownDeviceType",
                  deviceName: String = "unnamedDevice",
                  hwDeviceId: String = "unknownHwDeviceId",
                  hashedHwDeviceId: String = "unknownHwDeviceId",
                  tags: Set[String] = Set(),
                  deviceConfig: Option[JValue] = None,
                  deviceProperties: Option[JValue] = None,
                  subQueues: Option[Set[String]] = None,
                  pubQueues: Option[Set[String]] = Some(Set(Config.awsSqsQueueTransformerOut)),
                  avatarLastUpdated: Option[DateTime] = None,
                  deviceLastUpdated: Option[DateTime] = None,
                  updated: Option[DateTime] = None,
                  created: DateTime = DateTime.now(DateTimeZone.UTC)
                 ) {

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case dev: Device =>
        if (
          dev.deviceId == this.deviceId
        )
          true
        else
          false
      case _ => false
    }
  }

  override def hashCode(): Int = this.deviceId.hashCode
}

/**
  * we might use this draft in the future
  */
case class DeviceDraft(id: String,
                       name: Option[String],
                       hwType: Option[String],
                       hwId: Option[String],
                       syncState: Option[Int], // 0 = out of sync, 1 = in sync, 100 = unknown
                       tags: Option[Set[String]],
                       properties: Option[JValue],
                       subscriptions: Option[Set[String]],
                       config: Option[JValue],
                       avatar: Option[AvatarDraft],
                       created: Option[DateTime],
                       updated: Option[DateTime],
                       lastActive: Option[DateTime]
                      )

/**
  * we might use this draft in the future
  */
case class AvatarDraft(id: String,
                       stateDesired: Option[JValue],
                       stateReported: Option[JValue],
                       created: Option[DateTime],
                       updated: Option[DateTime],
                       lastActive: Option[DateTime]
                      )