package com.ubirch.transformer.actor

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.camel.{CamelMessage, Consumer}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.camel.CamelActorUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 30/10/16.
  */
class TransformerConsumerActor
  extends Consumer
    with CamelActorUtil
    with MyJsonProtocol
    with ActorLogging {

  val accessKey: String = Config.awsAccessKey
  val secretKey: String = Config.awsSecretAccessKey

  override def endpointUri: String = sqsEndpointConsumer(Config.sqsConfig(Config.awsSqsQueueTransformer))

  override def autoAck: Boolean = true

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  val transformerActor: ActorRef = context
    .actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers)
      .props(Props[TransformerPreprocessorActor]), ActorNames.TRANSFORMER_PRE)

  //TODO fix error handling, in case of error the message should be resend later?
  override def receive: Receive = {
    case msg: CamelMessage =>
      log.debug(s"received ${msg.bodyAs[String]}")
      msg.body match {
        case drdStr: String =>
          Json4sUtil.string2JValue(drdStr) match {
            case Some(drdJson) =>
              drdJson.extractOpt[DeviceDataRaw] match {
                case Some(drd) =>
                  DeviceManager.infoByHashedHwId(drd.a).map {
                    case Some(device) =>
                      transformerActor ! (device, drd)
                    case None =>
                      log.error(s"no device found for hashedHwdeviceId: ${drd.a}")
                  }
                case None =>
                  log.error(s"invalid json message from device: $drdStr")
              }
            case None =>
              log.error(s"invalid message from device: $drdStr")
          }

        case _ =>
          log.error(s"received invalid message body: ${msg.body}")
      }

    case _ =>
      log.error("received unknown message")
  }
}

object TransformerConsumerActor {
  def props: Props = Props[TransformerConsumerActor]
}