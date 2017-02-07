package com.ubirch.transformer.actor

import akka.actor.{ActorLogging, Props}
import akka.camel.{CamelMessage, Consumer}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceDataProcessed
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

/**
  * Created by derMicha on 30/10/16.
  */
class MqttConsumerActor
  extends Consumer
    with ActorLogging
    with MyJsonProtocol {

  val mqttUser = Config.mqttUser

  val mqttPassword = Config.mqttPassword

  override def endpointUri = s"paho://${Config.awsSqsQueueTransformerOut}?accessKey=$mqttUser&secretKey=$mqttPassword&delaySeconds=10"

  override def autoAck: Boolean = true

  val transformerActor = context.actorOf(new RoundRobinPool(3).props(Props[TransformerPreprocessorActor]), "transformer-pre-processor-actor")

  implicit val executionContext = context.dispatcher

  //TODO fix error handling, in case of error the message should be resend later?
  override def receive = {
    case msg: CamelMessage =>
      msg.body match {
        case dataStr: String =>
          try {
            Json4sUtil.string2JValue(dataStr) match {
              case Some(dataJval) =>
                dataJval.extractOpt[DeviceDataProcessed] match {
                  case Some(ddp) =>
                    transformerActor ! ddp
                  case None =>
                    log.error(s"json parser failed for message: $dataStr")
                }
              case None =>

            }
          } catch {
            case e: Exception =>
              log.error(s"received invalid data: $dataStr", e)
          }
        case _ =>
          log.error(s"received invalid message body: ${msg.body}")
      }
      log.debug(s"received ${msg.bodyAs[String]}")
    case _ =>
      log.error("received unknown message")
  }
}
