package com.ubirch.transformer.model

/**
  * Created by derMicha on 24/05/17.
  */
case class MessageReceiver(topic: String, message: String, target: String) {
  def getKey = s"$target -> $topic"
}