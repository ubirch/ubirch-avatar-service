package com.ubirch.transformer.model

/**
  *
  * @param topic   message queue name
  * @param message message as string (json -> string)
  * @param target  current supported values: ConfigKeys.DEVICEOUTBOX
  */
case class MessageReceiver(
                            topic: String,
                            message: String,
                            target: String) {
  def getKey = s"${target}__${topic}"
}