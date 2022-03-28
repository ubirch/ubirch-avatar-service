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

  val pathCheck = s"$pathPrefix/$check"
  val pathDeepCheck = s"$pathPrefix/$deepCheck"

  val pathDevice = s"$pathPrefix/$device"

  val pathDeviceBulk = s"$pathDevice/$update/$bulk"
  val pathDeviceType = s"$pathDevice/$deviceType"
  val pathDeviceTypeInit = s"$pathDeviceType/$init"

  def pathDeviceWithId(id: String): String = s"$pathDevice/$id"

  val pathDeviceStub = s"$pathDevice/$stub"

  def pathDeviceStubWithId(id: String): String = s"$pathDeviceStub/$id"

  val pathDeviceClaim = s"$pathDevice/$claim"

  def pathDeviceState(id: String): String = s"${pathDeviceWithId(id)}/$state"

  val pathDeviceDataRaw: String = s"$pathDevice/$data/$raw"
  def pathDeviceDataTransferDates(deviceId: String): String = s"$pathDevice/$data/$transferDates/$deviceId"

  def pathDeviceDataHistory(id: String): String = s"${pathDeviceWithId(id)}/$data/$history"

  val pathDeviceUpdate: String = s"$pathDevice/$update"

  def pathDeviceHistoryFrom(id: String, from: Int): String = s"${pathDeviceDataHistory(id)}/$from"

  def pathDeviceHistoryFromSize(id: String, from: Int, size: Int): String = s"${pathDeviceDataHistory(id)}/$from/$size"

  def pathDeviceHistoryByDatePrefix(id: String): String = s"${pathDeviceDataHistory(id)}/$byDate"

  def pathDeviceHistoryByDateFromTo(id: String, fromDate: String, toDate: String): String = s"${pathDeviceHistoryByDatePrefix(id)}/$from/$fromDate/$to/$toDate"

  def pathDeviceHistoryByDateBefore(id: String, beforeDate: String): String = s"${pathDeviceHistoryByDatePrefix(id)}/$before/$beforeDate"

  def pathDeviceHistoryByDateAfter(id: String, afterDate: String): String = s"${pathDeviceHistoryByDatePrefix(id)}/$after/$afterDate"

  def pathDeviceHistoryByDateDay(id: String, dayDate: String): String = s"${pathDeviceHistoryByDatePrefix(id)}/$day/$dayDate"

}
