package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.Producer
import com.ubirch.avatar.config.Config
import com.ubirch.util.uuid.UUIDUtil

/**
  * Created by derMicha on 24/02/17.
  */
class DeviceMessageProcessedActor(deviceUuid: String)
  extends Actor
    with (Producer)
    with ActorLogging {

  val mqttUser: String = Config.mqttUser

  val mqttPassword: String = Config.mqttPassword

  val mqttBrokerUrl: String = Config.mqttBrokerUrl

  val mqttDeviceOutTopic: String = s"${Config.mqttTopicDevicesBase}/$deviceUuid/${Config.mqttTopicDevicesProcessed}"

  val clientId: String = s"avatarService_${UUIDUtil.uuidStr}"

  override def endpointUri: String = s"mqtt:" +
    s"DeviceMessageProcessedActor?host=$mqttBrokerUrl&publishTopicName=$mqttDeviceOutTopic&userName=$mqttUser&password=$mqttPassword"

}

object DeviceMessageProcessedActor {
  def props(deviceUuid: String): Props = Props(new DeviceMessageProcessedActor(deviceUuid))
}
