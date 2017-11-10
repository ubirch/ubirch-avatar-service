package com.ubirch.avatar.model.rest.payload

import java.util.UUID

import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{DateTime, DateTimeZone}

/**
  * Created by derMicha on 01/02/17.
  */

/**
  * @param cy measurement cycles
  * @param t  temperature
  * @param er error code
  * @param ts Timestamp of measurement
  */
final case class TrackleSensorPayload(
                                       cy: Long = 0,
                                       t: Int,
                                       er: Int = 0,
                                       ts: DateTime = DateTime.now(DateTimeZone.UTC)
                                     )

/**
  * @param mid measurementid
  * @param did tarckle device id
  * @param ts  Timestamp of measurement
  * @param te  temperature
  * @param er  error code
  */
final case class TrackleSensorMeasurement(
                                           mid: UUID = UUIDUtil.uuid,
                                           pid: Option[UUID] = None,
                                           did: String,
                                           ts: DateTime = DateTime.now(DateTimeZone.UTC),
                                           te: BigDecimal,
                                           er: Int = 0
                                         )
