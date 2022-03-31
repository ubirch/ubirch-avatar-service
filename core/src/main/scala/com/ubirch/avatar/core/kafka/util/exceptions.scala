package com.ubirch.avatar.core.kafka.util

/**
  * ex. json parser error
  */
case class InvalidDataException(message: String) extends Exception(message)

/**
  * ex. network error
  */
case class UnexpectedException(message: String) extends Exception(message)

case class JsonParseException(message: String) extends Exception(message)
