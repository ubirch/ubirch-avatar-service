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
  * @param v      message type version
  * @param a      hashed hardware deviceId
  * @param k      public key
  * @param ts     timestamp
  * @param s      hashed auth token or public key
  * @param p      payload
  * @param txHash optional hash of related blockchain anchor
  */
case class DeviceDataRaw(
                          id: UUID = UUIDUtil.uuid, // messageId
                          v: String = MessageVersion.v003,
                          a: String,
                          ts: DateTime = DateTime.now(),
                          k: Option[String] = None,
                          s: Option[String] = None,
                          uuid: Option[String] = None,
                          p: JValue,
                          deviceId: Option[String] = None,
                          deviceName: Option[String] = None,
                          chainedHash: Option[String] = None, // MD5 hash of signature
                          txHash: Option[String] = None, // related bitcoin transaction hash
                          txHashLink: Option[String] = None, // related chain explorer url
                          txHashLinkHtml: Option[String] = None // related chain explorer html-link
                        ) {
  override def hashCode(): Int = id.hashCode()

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case sdm: DeviceDataRaw if sdm.id == this.id =>
        true
      case _ =>
        false

    }
  }

  override def toString: String = s"id: $id / v: $v / p: $p / txhash: $txHash"

}

case class DeviceDataRawEnvelope(
                                  validationState: String,
                                  deviceMessage: DeviceDataRaw,
                                  signature: Option[String],
                                  timestamp: DateTime = DateTime.now()
                                )

case class PayloadV3(c: JValue, p: Array[JValue])
