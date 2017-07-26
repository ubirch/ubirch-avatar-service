package com.ubirch.avatar.mqttbridge

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.actor.MqttDeviceConsumerActor

/**
  * Created by derMicha on 26/06/17.
  */
object Boot extends App
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  logger.info("ubirchAvatarService MQTTBridge started")

  system.actorOf(Props(new MqttDeviceConsumerActor()), ActorNames.MQTT_CONSUMER)

}
