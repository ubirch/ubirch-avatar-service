package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.camel.CamelMessage
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.{Config, ConfigKeys, Const}
import com.ubirch.avatar.core.avatar.AvatarStateManagerREST
import com.ubirch.avatar.core.device.{DeviceManager, DeviceStateManager}
import com.ubirch.avatar.model.actors.MessageReceiver
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.transformer.actor.TransformerProducerActor
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import io.prometheus.client.Histogram
import org.json4s.JValue

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

/**
  * author: derMicha
  * since: 2016-10-28
  */
class MessageProcessorActor(implicit mongo: MongoUtil)
  extends Actor
    with MyJsonProtocol
    with ActorLogging {

  private implicit val exContext: ExecutionContextExecutor = context.dispatcher

  private val transformerActor: ActorRef = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[TransformerProducerActor]), ActorNames.TRANSFORMER_PRODUCER)

  private val persistenceActor: ActorRef = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[MessagePersistenceActor]), ActorNames.PERSISTENCE_SVC)

  private val notaryActor: ActorRef = context.actorOf(Props[MessageNotaryActor], ActorNames.NOTARY_SVC)

  private val chainActor: ActorRef = context.actorOf(Props[MessageChainActor], ActorNames.CHAIN_SVC)

  private val outboxManagerActor: ActorRef = context.actorOf(Props[DeviceOutboxManagerActor], ActorNames.DEVICE_OUTBOX_MANAGER)

  override def receive: Receive = {

    case (s: ActorRef, drd: DeviceDataRaw, device: Device) =>

      log.debug(s"received for deviceId ${device.deviceId} message: $drd")

      if (DeviceManager.checkProperty(device, Const.STOREDATA)) {
        log.debug(s"stores data for ${device.deviceId}")
        persistenceActor ! drd
      }
      else
        log.debug(s"stores no data for ${device.deviceId}")

      if (DeviceCoreUtil.checkNotaryUsage(device)) {
        log.debug(s"use notary service for ${device.deviceId}")
        notaryActor ! drd
      }
      else
        log.debug(s"do not use notary service for ${device.deviceId}")

      if (DeviceManager.checkProperty(device, Const.CHAINDATA) || DeviceManager.checkProperty(device, Const.CHAINHASHEDDATA)) {
        log.debug(s"chain data: ${device.deviceId}")
        chainActor ! (drd, device)
      }
      else
        log.debug(s"do not chain data for ${device.deviceId}")

      (drd.v match {
        case MessageVersion.`v40` =>
          drd.p.extract[Array[JValue]].map { payload =>
            processPayload(device, payload)
          }.toList.reverse.head
        case MessageVersion.`v003` =>
          drd.p.extract[Array[JValue]].map { payload =>
            processPayload(device, payload)
          }.toList.reverse.head
        case _ =>
          processPayload(device, drd.p)
      }).map {
        case Some(d) =>
          s ! d
          val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(d).get)
          DeviceManager.infoByHashedHwId(drd.a).map {
            case Some(device) =>
              outboxManagerActor ! MessageReceiver(device.deviceId, currentStateStr, ConfigKeys.DEVICEOUTBOX)
            case None =>
              log.error("lookup device by hasedHwDeviceId failed")
          }
        case None =>
          log.error(s"current AvatarStateRest not available: ${device.deviceId}")
          s ! JsonErrorResponse(errorType = "AvatarState Error", errorMessage = s"Could not get current Avatar State Rest for ${device.deviceId}")
      }

      Json4sUtil.any2jvalue(drd) match {
        case Some(drdJson) =>
          transformerActor ! Json4sUtil.jvalue2String(drdJson)
        case None =>
          log.error(s"could not create json for message: ${drd.id}")
      }

    case msg: CamelMessage =>
      //@TODO check why we receive here CamelMessages ???
      log.debug(s"received CamelMessage")

    case _ => log.error("received unknown message")

  }

  private def processPayload(device: Device, payload: JValue): Future[Option[DeviceStateUpdate]] = {
    val requestTimer: Histogram.Timer = MessageProcessorActor.requestLatency.startTimer
    AvatarStateManagerREST.setReported(restDevice = device, payload) map {
      case Some(currentAvatarState) =>
        val dsu = DeviceStateManager.createNewDeviceState(device, currentAvatarState)
        DeviceStateManager.upsert(state = dsu)
        requestTimer.observeDuration()
        Some(dsu)
      case None =>
        log.error(s"Could not get current Avatar State for ${device.deviceId}")
        requestTimer.observeDuration()
        None
    }
  }

}

object MessageProcessorActor {
  private val requestLatency: Histogram = Histogram
    .build()
    .name("akka_processState_seconds")
    .help("Akka process state latency in seconds.")
    .register()
}