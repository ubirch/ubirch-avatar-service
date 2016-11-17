package com.ubirch.transformer.actor

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.camel.{CamelMessage, Consumer}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.{DeviceDataRawManager, DeviceManager}

/**
  * Created by derMicha on 30/10/16.
  */
class AwsConsumerActor extends Consumer with ActorLogging {

  val accessKey = System.getenv().get(Config.awsAccessKey)

  val secretKey = System.getenv().get(Config.awsSecretAccessKey)

  override def endpointUri = s"aws-sqs://${Config.awsSqsQueueTransformer}?accessKey=$accessKey&secretKey=$secretKey"

  override def autoAck: Boolean = false

  val transformerActor = context.actorOf(Props[TransformerPreprocessorActor], "transformer-pre-actor")

  implicit val executionContext = context.dispatcher


  //TODO fix error handling, in case of errora the message should be resend later?
  override def receive = {
    case msg: CamelMessage =>
      msg.body match {
        case idStr: String =>
          try {
            val id = UUID.fromString(idStr)
            log.debug(s"received id: $id")
            DeviceDataRawManager.history(id = id).map {
              case Some(drd) =>
                DeviceManager.infoByHashedHwId(drd.a).map {
                  case Some(device) =>
                    transformerActor ! (device, drd)
                  case None =>
                    log.error(s"no device found for hashedHwdeviceId: ${drd.a}")
                }

              case _ =>
                log.error(s"no raw data found for id: $id")
            }

          } catch {
            case e: Exception =>
              log.error(s"received invalid id: $idStr", e)
          }
        case _ =>
          log.error(s"received invalid message body: ${msg.body}")
      }
      log.debug(s"received ${msg.bodyAs[String]}")
    case _ =>
      log.error("received unknown message")
  }
}
