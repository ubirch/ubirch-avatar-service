package com.ubirch.avatar.server.util

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
  val devicestub = "devicestub"
  val state = "state"
  val history = "history"
  val update = "update"
  val bulk = "bulk"
  val data = "data"
  val raw = "raw"
  val deviceType = "deviceType"
  val init = "init"

  val urlPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val urlDevice = s"$urlPrefix/$device"

  val urlDeviceBulk = s"$urlDevice/$bulk"
  val urlDeviceType = s"$urlDevice/$deviceType"
  val urlDeviceTypeInit = s"$urlDeviceType/$init"

  def urlDeviceWithId(id: String): String = s"$urlDevice/$id"

  val urlDeviceStubWithIdPrefix = s"$urlDevice/$stub"
  def urlDeviceStubWithId(id: String): String = s"$urlDeviceStubWithIdPrefix/$id"

  def urlDeviceState(id: String): String = s"${urlDeviceWithId(id)}/$state"
  val urlDeviceDataRaw: String = s"$urlDevice/$data/$raw"
  def urlDeviceDataHistory(id: String): String = s"${urlDeviceWithId(id)}/$data/$history"
  val urlDeviceUpdate: String = s"$urlDevice/$update"
  def urlDeviceHistoryFrom(id: String, from: Int): String = s"${urlDeviceDataHistory(id)}/$from"
  def urlDeviceHistoryFromSize(id: String, from: Int, size: Int): String = s"${urlDeviceDataHistory(id)}/$from/$size"

}
