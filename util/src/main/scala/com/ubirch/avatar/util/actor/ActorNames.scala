package com.ubirch.avatar.util.actor

/**
  * author: cvandrei
  * since: 2017-02-20
  */
object ActorNames {

  final val HISTORY: String = "AVS-device-data-history-actor"

  final val REPROCESSING: String = "AVS-device-data-reprocessing-actor"

  final val TRANSFORMER_PRODUCER: String = "AVS-transformer-producer"

  final val TRANSFORMER_CONSUMER: String = "AVS-transformer-consumer"

  final val TRANSFORMER_PRE: String = "AVS-transformer-pre-actor"

  final val TRANSFORMER_POST: String = "AVS-transformer-post-actor"

  final val TRANSFORMER_OUTBOX_MANAGER: String = "AVS-transformer-outbox-manager-actor"

  final val DEVICE_OUTBOX_MANAGER: String = "AVS-device-outbox-manager-actor"

  final val DEVICE_INBOX: String = "AVS-device-inbox-actor"

  final val DEVICE_OUTBOX_MANAGER_PATH: String = s"/user/$DEVICE_OUTBOX_MANAGER"

  final val MQTT_CONSUMER: String = "AVS-mqtt-consumer"

  final val PERSISTENCE_SVC: String = "AVS-persistence-service"

  final val DEVICESTATEUPDATER: String = "AVS-devicestateupdate-service"

  final val MSG_PROCESSOR: String = "AVS-message-processor"

  final val MSG_PROCESSOR_PATH: String = s"/user/$MSG_PROCESSOR"

  final val REPLAY_FILTER: String = "AVS-replay-filter"

  final val REPLAY_FILTER_PATH: String = s"/user/$REPLAY_FILTER"

  final val MSG_VALIDATOR: String = "AVS-message-validator"

  final val MSG_VALIDATOR_PATH: String = s"/user/$MSG_VALIDATOR"

  final val MSG_MSGPACK_PROCESSOR: String = "AVS-message-msgpack-processor"

  final val MSG_MSGPACK_PROCESSOR_PATH: String = s"/user/$MSG_MSGPACK_PROCESSOR"

  final val DEVICE_API: String = "AVS-device-api"

  final val DEVICE_API_PATH: String = s"/user/$DEVICE_API"

  final val CHAIN_SVC: String = "AVS-chain-service"

  final val OUT_PRODUCER: String = "AVS-out-producer"

  final val DEEP_CHECK = "AVS-deep-check-actor"

}
