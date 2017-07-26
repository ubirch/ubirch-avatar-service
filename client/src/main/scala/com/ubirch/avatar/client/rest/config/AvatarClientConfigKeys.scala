package com.ubirch.avatar.client.rest.config

/**
  * author: cvandrei
  * since: 2017-07-25
  */
object AvatarClientConfigKeys {

  private val prefix = "ubirchAvatarService.client.rest"

  final val USER_TOKEN = s"$prefix.userToken"

  final val BASE_URL = s"$prefix.baseUrl"

  private val prefixTimeout = s"$prefix.timeout"

  final val TIMEOUT_CONNECT = s"$prefixTimeout.connect"

  final val TIMEOUT_READ = s"$prefixTimeout.read"

}
