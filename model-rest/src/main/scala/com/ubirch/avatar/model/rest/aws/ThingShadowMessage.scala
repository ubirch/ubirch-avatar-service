package com.ubirch.avatar.model.rest.aws

import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JValue

/**
  * Created by derMicha on 21/04/16.
  */
case class ThingShadowMessage(
                               state: ThingShadowState
                             )


case class ThingShadowState(
                             inSync: Option[Boolean] = None,
                             desired: Option[JValue] = None,
                             reported: Option[JValue] = None,
                             delta: Option[JValue] = None,
                             deviceLastUpdated: Option[DateTime] = Some(DateTime.now()),
                             avatarLastUpdated: Option[DateTime] = Some(DateTime.now())
                           )
