package com.ubirch.avatar.model.device

import java.util.UUID

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
  * @param a  hashed deviceId
  * @param k  public key
  * @param ts timestamp
  * @param s  hashed auth token or public key
  * @param p  payload
  */
case class SimpleDeviceMessage(
                                id: UUID = UUIDUtil.uuid, // messageId
                                v: String = "0.0.3",
                                a: Option[String] = None,
                                k: Option[String] = None,
                                ts: Option[DateTime] = None,
                                s: String,
                                p: JValue
                              ) {
  override def hashCode(): Int = id.hashCode()

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case sdm: SimpleDeviceMessage if sdm.id == this.id =>
        true
      case _ =>
        false

    }
  }

  override def toString: String = s"id: $id / v: $v / p: $p"
}

case class SimpleDeviceMessageEnvelope(
                                        validationState: String,
                                        deviceMessage: SimpleDeviceMessage,
                                        signature: Option[String],
                                        timestamp: DateTime = DateTime.now()
                                      )

case class PayloadV3(c: JValue, p: Array[JValue])
