package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.{Config, Const}
import com.ubirch.avatar.core.avatar.AvatarStateManagerREST
import com.ubirch.avatar.core.device.{DeviceManager, DeviceStateManager}
import com.ubirch.avatar.core.prometheus.Timer
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.json4s.{JValue, MappingException}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: derMicha
  * since: 2016-10-28
  */
class MessageProcessorActor(implicit mongo: MongoUtil)
  extends Actor
    with MyJsonProtocol
    with ActorLogging {

  private implicit val exContext: ExecutionContextExecutor = context.dispatcher

  private val persistenceActor: ActorRef = context.actorOf(MessagePersistenceActor.props, ActorNames.PERSISTENCE_SVC)

  private val notaryActor: ActorRef = context.actorOf(MessageNotaryActor.props, ActorNames.NOTARY_SVC)

  private val chainActor: ActorRef = context.actorOf(Props[MessageChainActor], ActorNames.CHAIN_SVC)

  private val outboxManagerActor = context.
    actorSelection(ActorNames.DEVICE_OUTBOX_MANAGER_PATH)

  private val processStateTimer = new Timer(s"process_state_${scala.util.Random.nextInt(100000)}")

  override def receive: Receive = {

    case (drd: DeviceDataRaw, device: Device) =>
      val s = context.sender()

      val drdPatched = drd.copy(
        deviceName = Some(device.deviceName),
        deviceType = Some(device.deviceTypeKey),
        deviceId = Some(device.deviceId),
        tags = Some(device.tags)
      )

      log.debug(s"received for deviceId ${device.deviceId} message: $drdPatched")

      //manage new device state
      val pl = try {
        drdPatched.p.extract[Array[JValue]].foldLeft[JValue](Json4sUtil.string2JValue("{}").get) { (a, b) =>
          a merge b
        }
      }
      catch {
        case e: MappingException =>
          drdPatched.p.extract[JValue]

      }

      processPayload(device, pl, drdPatched.s).onComplete {
        case Success(Some(d)) =>
          s ! d
          val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(d).get)
        //@TODO just a hot fix
        //outboxManagerActor ! MessageReceiver(device.deviceId, currentStateStr, ConfigKeys.DEVICEOUTBOX)
        case Success(None) =>
          log.error(s"current AvatarStateRest not available: ${device.deviceId}")
          val jer = JsonErrorResponse(errorType = "AvatarState Error", errorMessage = s"Could not get current Avatar State Rest for ${device.deviceId}")
          //@TODO just a hot fix
          //outboxManagerActor ! MessageReceiver(device.deviceId, jer.toJsonString, ConfigKeys.DEVICEOUTBOX)
          s ! jer
        case Failure(t) =>
          log.error(s"current AvatarStateRest not available: ${device.deviceId}")
          val jer = JsonErrorResponse(errorType = "AvatarState Error", errorMessage = t.getMessage)
          //@TODO just a hot fix
          //outboxManagerActor ! MessageReceiver(device.deviceId, jer.toJsonString, ConfigKeys.DEVICEOUTBOX)
          s ! jer
      }

      if (DeviceManager.checkProperty(device, Const.STOREDATA)) {
        log.debug(s"stores data for ${device.deviceId}")
        persistenceActor ! drdPatched
      }
      else
        log.debug(s"stores no data for ${device.deviceId}")

      if (DeviceCoreUtil.checkNotaryUsage(device)) {
        log.debug(s"use notary service for ${device.deviceId}")
        notaryActor ! drdPatched
      }
      else
        log.debug(s"do not use notary service for ${device.deviceId}")

      if (DeviceManager.checkProperty(device, Const.CHAINDATA) || DeviceManager.checkProperty(device, Const.CHAINHASHEDDATA)) {
        log.debug(s"chain data: ${device.deviceId}")
        chainActor ! (drdPatched, device)
      }
      else
        log.debug(s"do not chain data for ${device.deviceId}")

      // publish incomming raw data
      outboxManagerActor ! (device, drdPatched)
  }

  private def processPayload(device: Device, payload: JValue, signature: Option[String] = None): Future[Option[DeviceStateUpdate]] = {
    processStateTimer.start
    AvatarStateManagerREST.setReported(restDevice = device, payload, signature) map {
      case Some(currentAvatarState) =>
        val dsu = DeviceStateManager.createNewDeviceState(device, currentAvatarState)
        DeviceStateManager.upsert(state = dsu)
        processStateTimer.stop
        Some(dsu)
      case None =>
        log.error(s"Could not get current Avatar State for ${device.deviceId}")
        processStateTimer.stop
        None
    }
  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received unknown message: $message")
  }
}

object MessageProcessorActor {
  def props()(implicit mongo: MongoUtil): Props = new RoundRobinPool(Config.akkaNumberOfFrontendWorkers)
    .props(Props(new MessageProcessorActor()))
}