package com.ubirch.avatar.core.server.util

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object RouteConstants {

  val apiPrefix = "api"
  val currentVersion = "v1"
  val serviceName = "avatarService"
  val device = "device"
  val stub = "stub"
  val history = "history"

  val urlPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val urlDevice = s"$urlPrefix/$device"

  def urlDeviceWithId(id: String): String = s"$urlDevice/$id"

  val urlDeviceStubWithIdPrefix = s"$urlDevice/$stub"
  def urlDeviceStubWithId(id: String): String = s"$urlDeviceStubWithIdPrefix/$id"

  def urlDeviceHistory(id: String): String = s"${urlDeviceWithId(id)}/$history"
  def urlDeviceHistoryFrom(id: String, from: Long): String = s"${urlDeviceHistory(id)}/$from"
  def urlDeviceHistoryFromSize(id: String, from: Long, size: Long): String = s"${urlDeviceHistory(id)}/$from/$size"


}
