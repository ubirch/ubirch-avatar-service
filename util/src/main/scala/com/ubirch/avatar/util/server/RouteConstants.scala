package com.ubirch.avatar.util.server

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

  val pathPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val pathDevice = s"$pathPrefix/$device"

  val pathDeviceBulk = s"$pathDevice/$bulk"
  val pathDeviceType = s"$pathDevice/$deviceType"
  val pathDeviceTypeInit = s"$pathDeviceType/$init"

  def pathDeviceWithId(id: String): String = s"$pathDevice/$id"

  val pathDeviceStubWithIdPrefix = s"$pathDevice/$stub"
  def pathDeviceStubWithId(id: String): String = s"$pathDeviceStubWithIdPrefix/$id"

  def pathDeviceState(id: String): String = s"${pathDeviceWithId(id)}/$state"
  val pathDeviceDataRaw: String = s"$pathDevice/$data/$raw"
  def pathDeviceDataHistory(id: String): String = s"${pathDeviceWithId(id)}/$data/$history"
  val pathDeviceUpdate: String = s"$pathDevice/$update"
  def pathDeviceHistoryFrom(id: String, from: Int): String = s"${pathDeviceDataHistory(id)}/$from"
  def pathDeviceHistoryFromSize(id: String, from: Int, size: Int): String = s"${pathDeviceDataHistory(id)}/$from/$size"

}