package com.ubirch.transformer.actor

import akka.actor.{ActorLogging, Props}
import akka.camel.{CamelMessage, Consumer}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.MessageValidatorActor
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 30/10/16.
  */

class MqttDeviceConsumerActor
  extends Consumer
    with ActorLogging
    with MyJsonProtocol {

  val mqttUser: String = Config.mqttUser
  val mqttPassword: String = Config.mqttPassword
  val mqttBrokerUrl: String = Config.mqttBrokerUrl

  val clientId = s"avatarService_${UUIDUtil.uuidStr}"

  override def endpointUri = s"paho:${Config.mqttQueueDevicesIn}?clientId=$clientId&brokerUrl=$mqttBrokerUrl"

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  private val validatorActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[MessageValidatorActor]), ActorNames.MSG_VALIDATOR)

  override def preStart(): Unit = {
    super.preStart()
    log.info(s"listening to mqtt broker: $mqttBrokerUrl")
  }

  //TODO fix error handling, in case of error the message should be resend later?
  override def receive: PartialFunction[Any, Unit] = {
    case msg: CamelMessage =>
      msg.body match {
        case dataStr: String =>
          self ! dataStr
        case bytes: Array[Byte] =>
          val dataStr = new String(bytes, "UTF-8")
          self ! dataStr
        case _ =>
          log.error(s"received invalid message body: ${msg.body}")
      }
    //      log.debug(s"received mqtt message: ${msg.bodyAs[String]}")
    case msgStr: String =>
      try {
        Json4sUtil.string2JValue(msgStr) match {
          case Some(jval) =>
            jval.extractOpt[DeviceDataRaw] match {
              case Some(ddr) =>
                validatorActor ! ddr
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
    case _ =>
      log.error("received unknown message")
  }
}
