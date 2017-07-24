package com.ubirch.avatar.cmd.config

import com.ubirch.util.config.ConfigBase

/**
  * author: cvandrei
  * since: 2017-07-24
  */
object AvatarCmdToolsConfig extends ConfigBase {

  def userToken: Option[String] = {

    val key = AvatarCmdToolsConfigKeys.USER_TOKEN

    if (config.hasPath(key)) {
      Some(config.getString(key))
    } else {
      None
    }

  }

  def avatarBaseUrl: String = {

    val key = AvatarCmdToolsConfigKeys.AVATAR_BASE_URL

    if (config.hasPath(key)) {
      config.getString(key)
    } else {
      "http://localhost:8080"
    }

  }

}
