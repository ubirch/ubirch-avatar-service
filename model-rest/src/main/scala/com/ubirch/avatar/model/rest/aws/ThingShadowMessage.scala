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

/**
  * @param deviceId          id of associated device, should be a UUID
  * @param inSync            means reported == desired
  * @param desired           json that contains changed device config and states from other sensors (aktor -> sensor)
  * @param reported          last state which the decives has reported
  * @param delta             delta state = reported - desired
  * @param deviceLastUpdated timestamp of last reported state update
  * @param avatarLastUpdated timestamp of last desired state update
  */
case class AvatarState(
                        deviceId: UUID,
                        inSync: Option[Boolean] = None,
                        desired: Option[JValue] = None,
                        reported: Option[JValue] = None,
                        delta: Option[JValue] = None,
                        // update on device
                        deviceLastUpdated: Option[DateTime] = Some(DateTime.now(DateTimeZone.UTC)),
                        // update on server side
                        avatarLastUpdated: Option[DateTime] = Some(DateTime.now(DateTimeZone.UTC))
                      )
