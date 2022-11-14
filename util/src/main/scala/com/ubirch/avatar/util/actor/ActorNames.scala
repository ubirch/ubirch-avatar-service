package com.ubirch.avatar.util.actor

/**
  * author: cvandrei
  * since: 2017-02-20
  */
object ActorNames {

  final val DEVICE_OUTBOX_MANAGER: String = "AVS-device-outbox-manager-actor"

  final val DEVICE_OUTBOX_MANAGER_PATH: String = s"/user/$DEVICE_OUTBOX_MANAGER"

  final val MSG_PROCESSOR: String = "AVS-message-processor"

  final val MSG_PROCESSOR_PATH: String = s"/user/$MSG_PROCESSOR"

  final val MSG_VALIDATOR: String = "AVS-message-validator"

  final val MSG_VALIDATOR_PATH: String = s"/user/$MSG_VALIDATOR"

  final val MSG_MSGPACK_PROCESSOR: String = "AVS-message-msgpack-processor"

  final val MSG_MSGPACK_PROCESSOR_PATH: String = s"/user/$MSG_MSGPACK_PROCESSOR"

  final val DEVICE_API: String = "AVS-device-api"

  final val DEVICE_API_PATH: String = s"/user/$DEVICE_API"

  final val DEEP_CHECK = "AVS-deep-check-actor"

}
