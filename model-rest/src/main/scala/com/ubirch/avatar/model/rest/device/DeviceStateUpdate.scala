package com.ubirch.avatar.model.rest.device

import java.util.UUID

import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.DateTime
import org.json4s._

/**
  * Payload could contain e, which contains a error message
  *
  * author: derMicha
  * since: 2016-10-28
  *
  * @param v  message type version
  * @param k  public key
  * @param ts timestamp
  * @param s  hashed auth token or public key
  * @param p  payload
  */
case class DeviceStateUpdate(
                              id: UUID = UUIDUtil.uuid, // messageId
                              v: String = MessageVersion.v002,
                              k: String,
                              s: String,
                              p: JValue,
                              ts: DateTime = DateTime.now
                            ) {
  override def hashCode(): Int = id.hashCode()

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case dsu: DeviceStateUpdate if dsu.id == this.id =>
        true
      case _ =>
        false

    }
  }

  override def toString: String = s"id: $id / v: $v / p: $p"
}

case class DeviceDataV3(c: JValue, d: JValue) {
  override def hashCode(): Int = s"$c$d".hashCode()

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case dd: DeviceDataV3 if c.equals(dd.c) && d.equals(dd.d) =>
        true
      case _ =>
        false

    }
  }

  override def toString: String = s"c: $c / d: $d"
}