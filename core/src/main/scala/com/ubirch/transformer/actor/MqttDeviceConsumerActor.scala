package com.ubirch.transformer.actor

import akka.actor.{ActorLogging, Props}
import akka.camel.{CamelMessage, Consumer}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.MessageValidatorActor
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 30/10/16.
  */

class MqttDeviceConsumerActor()
  extends Consumer
    with ActorLogging
    with MyJsonProtocol {

  val mqttUser: String = Config.mqttUser

  val mqttPassword: String = Config.mqttPassword

  val mqttBrokerUrl: String = Config.mqttBrokerUrl

  val mqttDeviceInTopic: String = s"${Config.mqttTopicDevicesBase}/+/${Config.mqttTopicDevicesIn}"

  val qualityOfService = "ExactlyOnce"

  val clientId: String = s"avatarService_${Config.enviroment}"

  //  override def endpointUri = s"paho:${Config.mqttQueueDevicesIn}?clientId=$clientId&brokerUrl=$mqttBrokerUrl&qualityOfService=$qualityOfService"

  override def endpointUri = s"mqtt:" +
    s"MqttDeviceConsumerActor?host=$mqttBrokerUrl&subscribeTopicName=$mqttDeviceInTopic&clientId=$clientId&userName=$mqttUser&password=$mqttPassword&qualityOfService=$qualityOfService&cleanSession=false"

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def preStart(): Unit = {
    super.preStart()
    log.info(s"listening to mqtt broker: $mqttBrokerUrl")
  }

  //TODO fix error handling, in case of error the message should be resend later?
  override def receive = {
    case msg: CamelMessage =>
      if (msg.getHeaders.keySet().contains("CamelMQTTSubscribeTopic")) {
        val mqttTopic = msg.getHeaders.get("CamelMQTTSubscribeTopic").toString
        val deviceUuid = mqttTopic.replace(Config.mqttTopicDevicesBase, "").replace(Config.mqttTopicDevicesIn, "").replaceAll("/", "")

        msg.body match {
          case dataStr: String =>
            self ! (dataStr, deviceUuid)
          case bytes: Array[Byte] =>
            val dataStr = new String(bytes, "UTF-8")
            self ! (dataStr, deviceUuid)
          case _ =>
            log.error(s"received invalid message body: ${msg.body}")
        }
      }

    case (msgStr: String, deviceUuid: String) =>
      try {
        Json4sUtil.string2JValue(msgStr) match {
          case Some(jval) =>
            jval.extractOpt[DeviceDataRaw] match {
              case Some(ddr) =>

              //TODO forward to SQS consumer

              //validatorActor ! ddr.copy(uuid = Some(deviceUuid))
              case None =>
                log.error(s"message is not a valid DeviceDataRaw object: $msgStr")
            }
          case None =>
            log.error(s"invalid json message: $msgStr")
        }
      } catch {
        case e: Exception =>
          log.error(s"received invalid data: $msgStr", e)
      }

    case dsu: DeviceStateUpdate =>
      log.debug(s"received DeviceStateUpdate: ${dsu.toString}, nothing to do!")

    case _ =>
      val sender = context.sender()
      log.error(s"received from ${sender.getClass.toString} unknown message")
  }
}
