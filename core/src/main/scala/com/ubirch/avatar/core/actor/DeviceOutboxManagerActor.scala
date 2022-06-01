package com.ubirch.avatar.core.actor

import akka.actor.{ Actor, ActorLogging, Props, Terminated }
import akka.util.Timeout
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.kafka.KafkaProducer
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.util.json.Json4sUtil

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

/**
  * Created by derMicha on 24/05/17.
  */

class DeviceOutboxManagerActor extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  final val TRACTOR_BASE: String = "AVS-transformerproducer-actor-api-"

  final val TRACTOR_BASE_PATH: String = s"/user/$TRACTOR_BASE"

  final val DOMACTOR_BASE: String = "AVS-deviceoutboxmanager-actor-api-"

  final val DOMACTOR_BASE_PATH: String = s"/user/$DOMACTOR_BASE"

  private val kafkaProducer =
    KafkaProducer.create(Config.kafkaBoostrapServer, Config.kafkaTrackelMsgpackTopic, context.system)

  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  override def receive: Receive = {

    case (device: Device, drd: DeviceDataRaw) =>
      val s = sender()
      val drdExt = drd.copy(deviceId = Some(device.deviceId))

      Json4sUtil.any2String(drdExt) match {
        case Some(drdStr) =>
          kafkaProducer.send(drdStr) onComplete {
            case Success(_) =>
              log.info(
                s"succeeded to publish DeviceRawData to Kafka. deviceId: ${device.deviceId}, raw message: ${drd.id}")
              s ! true
            case Failure(err) =>
              log.error(s"failed to publish DeviceRawData to Kafka. deviceId: ${device.deviceId}, raw message: ${drd.id}, error: $err")
              s ! false
          }
        case None =>
          log.error(s"error sending for device ${device.deviceId} raw message ${drd.id}")
          s ! false
      }

    case Terminated(actorRef) =>
      log.warning("Actor {} terminated", actorRef)
  }

  override def unhandled(message: Any): Unit = {
    message match {
      case _ => log.error(s"${message.getClass.toString}")
    }
  }
}

object DeviceOutboxManagerActor {
  def props(): Props = Props(new DeviceOutboxManagerActor())
}
