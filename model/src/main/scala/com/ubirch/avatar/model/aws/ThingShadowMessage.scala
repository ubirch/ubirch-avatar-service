package com.ubirch.avatar.model.aws

import java.util.UUID

import org.joda.time.DateTime
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

case class AvatarState(
                        deviceId: UUID,
                        inSync: Option[Boolean] = None,
                        desired: Option[JValue] = None,
                        reported: Option[JValue] = None,
                        delta: Option[JValue] = None,
                        // update on device
                        deviceLastUpdated: Option[DateTime] = Some(DateTime.now()),
                        // update on server side
                        avatarLastUpdated: Option[DateTime] = Some(DateTime.now())
                      )
