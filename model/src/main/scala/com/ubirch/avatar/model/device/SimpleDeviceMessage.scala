package com.ubirch.avatar.model.device

import org.joda.time.DateTime
import org.json4s._

/**
  *
  * @param v message type version
  * @param a hashed deviceId
  * @param k public key
  * @param s hashed auth token or public key
  * @param p payload
  */
case class SimpleDeviceMessage(
                                v: String = "0.0.3",
                                a: Option[String] = None,
                                k: Option[String] = None,
                                ts: Option[DateTime] = None,
                                s: String,
                                p: JValue
                              )

case class PayloadV3(c: JValue, p: Array[JValue])