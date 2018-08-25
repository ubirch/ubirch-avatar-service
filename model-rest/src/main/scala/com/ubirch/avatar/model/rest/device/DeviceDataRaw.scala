package com.ubirch.avatar.model.rest.device

import java.util.UUID

import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.DateTime
import org.json4s._

case class DeviceDataRaws(ddrs: Set[DeviceDataRaw])

/**
  * Payload could contain e, which contains a error message
  *
  * author: derMicha
  * since: 2016-10-28
  *
  * @param id             (optional) message id (default: generated by server)
  * @param v              (optional) message type version (default: 0.0.3)
  * @param fw             (optional) (default: n.a.) // TODO add description
  * @param umv            (optional) ubirch protocol main version
  * @param usv            (optional) ubirch protocol sub version
  * @param a              hashed hardware deviceId: base64(sha512(hardwareDeviceId))
  * @param did            (optional) deviceHardwareId
  * @param ts             (optional) timestamp (default: now())
  * @param k              (optional) public key
  * @param s              (optional) hashed auth token or public key
  * @param ps             (optional) signature of message prior to this one
  * @param mpraw          (optional) raw hex encoded MessagePack message
  * @param mppay          (optional) raw hex encoded MessagePack payload
  * @param p              JSON payload
  * @param config         (optional) current device configuration as JSON
  * @param meta           (optional) JSON // TODO add description
  * @param deviceId       (optional) // TODO add description
  * @param deviceName     (optional) // TODO add description
  * @param txHash         (optional) hash of related blockchain anchor
  * @param txHashLink     (optional) related chain explorer text-link
  * @param txHashLinkHtml (optional) related chain explorer html-link
  * @param deviceType     (optional) type of device
  * @param tags           (optional) set of tags
  * @param refId          (optional) // TODO add description
  */
case class DeviceDataRaw(
                          id: UUID = UUIDUtil.uuid,
                          v: String = MessageVersion.v003,
                          fw: String = "n.a.",
                          umv: Option[Int] = None,
                          usv: Option[Int] = None,
                          a: String,
                          did: Option[String] = None,
                          ts: DateTime = DateTime.now(),
                          k: Option[String] = None,
                          s: Option[String] = None,
                          ps: Option[String] = None,
                          mpraw: Option[String] = None,
                          mppay: Option[String] = None,
                          p: JValue,
                          config: Option[JValue] = None,
                          meta: Option[JValue] = None,
                          deviceId: Option[String] = None,
                          deviceName: Option[String] = None,
                          txHash: Option[String] = None, // related bitcoin transaction hash
                          txHashLink: Option[String] = None, // related chain explorer url
                          txHashLinkHtml: Option[String] = None, // related chain explorer html-link
                          deviceType: Option[String] = None,
                          tags: Option[Set[String]] = None,
                          refId: Option[UUID] = None
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
