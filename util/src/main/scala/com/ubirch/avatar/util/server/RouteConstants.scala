package com.ubirch.avatar.util.server

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object RouteConstants {

  val apiPrefix = "api"
  val currentVersion = "v1"
  val serviceName = "avatarService"
  val check = "check"
  val deepCheck = "deepCheck"
  val readyCheck = "readyCheck"
  val liveCheck = "liveCheck"
  val device = "device"
  val json = "json"
  val stub = "stub"
  val claim = "claim"
  val devicestub = "devicestub"
  val state = "state"
  val history = "history"
  val byDate = "byDate"
  val from = "from"
  val to = "to"
  val before = "before"
  val after = "after"
  val day = "day"
  val update = "update"
  val bulk = "bulk"
  val mpack = "mpack"
  val mpacks = "mpacks"
  val transferDates = "transferDates"
  val data = "data"
  val raw = "raw"
  val verify = "verify"
  val deviceType = "deviceType"
  val init = "init"
  val backendinfo = "backendinfo"
  val pubkey = "pubkey"

  val pathPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val pathDevice = s"$pathPrefix/$device"

  val pathDeviceType = s"$pathDevice/$deviceType"

  def pathDeviceWithId(id: String): String = s"$pathDevice/$id"

  val pathDeviceStub = s"$pathDevice/$stub"

  def pathDeviceDataHistory(id: String): String = s"${pathDeviceWithId(id)}/$data/$history"

  def pathDeviceHistoryByDatePrefix(id: String): String = s"${pathDeviceDataHistory(id)}/$byDate"

}
