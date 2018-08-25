package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.Producer
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 24/02/17.
  */
class DeviceStateUpdateActor(deviceUuid: String)
  extends Actor
    with (Producer)
    with ActorLogging {

  val mqttUser: String = Config.mqttUser

  val mqttPassword: String = Config.mqttPassword

  val mqttBrokerUrl: String = Config.mqttBrokerUrl

  val mqttDeviceOutTopic: String = s"${Config.mqttTopicDevicesBase}/$deviceUuid/${Config.mqttTopicDevicesOut}"

  //val clientId: String = s"avs_${UUIDUtil.uuidStr}"

  override def endpointUri: String = s"mqtt:" +
    //  s"DeviceStateUpdateActor?host=$mqttBrokerUrl&publishTopicName=$mqttDeviceOutTopic&clientId=$clientId&userName=$mqttUser&password=$mqttPassword"
    s"DeviceStateUpdateActor?host=$mqttBrokerUrl&publishTopicName=$mqttDeviceOutTopic&userName=$mqttUser&password=$mqttPassword"
}

object DeviceStateUpdateActor {
  def props(deviceUuid: String): Props = Props(new DeviceStateUpdateActor(deviceUuid))
}
