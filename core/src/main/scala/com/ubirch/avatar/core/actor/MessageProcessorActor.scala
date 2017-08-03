package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.camel.CamelMessage
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.{Config, ConfigKeys, Const}
import com.ubirch.avatar.core.avatar.AvatarStateManagerREST
import com.ubirch.avatar.core.device.DeviceStateManager
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
import org.json4s.JValue

import scala.concurrent.Future
import scala.language.postfixOps

/**
  * author: derMicha
  * since: 2016-10-28
  */
class MessageProcessorActor(implicit mongo: MongoUtil)
  extends Actor
    with MyJsonProtocol
    with ActorLogging {

  private implicit val exContext = context.dispatcher

  private val transformerActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[TransformerProducerActor]), ActorNames.TRANSFORMER_PRODUCER)

  private val persistenceActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[MessagePersistenceActor]), ActorNames.PERSISTENCE_SVC)

  private val notaryActor = context.actorOf(Props[MessageNotaryActor], ActorNames.NOTARY_SVC)

  val outboxManagerActor: ActorRef = context.actorOf(Props[DeviceOutboxManagerActor], ActorNames.DEVICE_OUTBOX_MANAGER)

  override def receive: Receive = {

    case (s: ActorRef, drd: DeviceDataRaw, device: Device) =>

      log.debug(s"received message: $drd")

      if (device.checkProperty(Const.STOREDATA))
        persistenceActor ! drd
      else
        log.info(s"stores no data: ${device.deviceId}")

      if (DeviceCoreUtil.checkNotaryUsage(device))
        notaryActor ! drd
      else
        log.info(s"does not use the notary service: ${device.deviceId}")

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
          if (drd.did.isDefined) {
            val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(d).get)
            outboxManagerActor ! MessageReceiver(drd.did.get, currentStateStr, ConfigKeys.DEVICEOUTBOX)
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
    AvatarStateManagerREST.setReported(restDevice = device, payload) map {
      case Some(currentAvatarState) =>
        val dsu = DeviceStateManager.createNewDeviceState(device, currentAvatarState)
        DeviceStateManager.upsert(state = dsu)
        Some(dsu)
      case None =>
        log.error(s"Could not get current Avatar State for ${device.deviceId}")
        None
    }
  }

}