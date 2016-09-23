package com.ubirch.avatar.model

import org.joda.time.DateTime
import org.json4s.JValue

/**
  * author: cvandrei
  * since: 2016-09-23
  */
case class Device(id: String,
                  name: Option[String],
                  hwType: Option[String],
                  hwId: Option[String],
                  syncState: Option[Int], // 0 = out of sync, 1 = in sync, 100 = unknown
                  tags: Option[Set[String]],
                  properties: Option[JValue],
                  subscriptions: Option[Set[String]],
                  config: Option[JValue],
                  avatar: Option[Avatar],
                  created: Option[DateTime],
                  updated: Option[DateTime],
                  lastActive: Option[DateTime]
                 )

case class Avatar(id: String,
                  stateDesired: Option[JValue],
                  stateReported: Option[JValue],
                  created: Option[DateTime],
                  updated: Option[DateTime],
                  lastActive: Option[DateTime]
                 )