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
  val claim = "claim"
  val state = "state"
  val mpack = "mpack"
  val update = "update"
  val verify = "verify"
  val init = "init"
  val backendinfo = "backendinfo"
  val pubkey = "pubkey"

  val pathPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

}
