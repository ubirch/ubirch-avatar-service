package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.{Config, ConfigKeys, Const}
import com.ubirch.avatar.core.avatar.AvatarStateManagerREST
import com.ubirch.avatar.core.device.{DeviceManager, DeviceStateManager}
import com.ubirch.avatar.core.prometheus.Timer
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{AvatarState, DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.transformer.model.MessageReceiver
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.mongo.connection.MongoUtil
import org.json4s.{JValue, MappingException}

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

  private val persistenceActor: ActorRef = context.actorOf(MessagePersistenceActor.props, ActorNames.PERSISTENCE_SVC)

  private val notaryActor: ActorRef = context.actorOf(MessageNotaryActor.props, ActorNames.NOTARY_SVC)

  private val chainActor: ActorRef = context.actorOf(Props[MessageChainActor], ActorNames.CHAIN_SVC)

  private val outboxManagerActor = context.actorSelection(ActorNames.DEVICE_OUTBOX_MANAGER_PATH)

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

      log.debug(s"received for deviceId ${device.deviceId} message: device ${drdPatched.deviceId}")

      //manage new device state
      val pl = try {
        drdPatched.p.extract[Array[JValue]].foldLeft[JValue](Json4sUtil.string2JValue("{}").get) { (a, b) =>
          a merge b
        }
      } catch {
        case e: MappingException =>
          drdPatched.p.extract[JValue]
      }

      val deviceStateUpdate = processPayload(device, pl, drdPatched.s).map {
        case Some(d: DeviceStateUpdate) =>
          log.debug(s"current AvatarState updated: ${device.deviceId}")
          val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(d).get)
          outboxManagerActor ! MessageReceiver(device.deviceId, currentStateStr, ConfigKeys.DEVICEOUTBOX)
          d
        case None =>
          log.error(s"current AvatarStateRest not available: ${device.deviceId}")
          val d = DeviceStateManager.createNewDeviceState(device, AvatarState(deviceId = device.deviceId, inSync = Some(false), currentDeviceSignature = drdPatched.s))
          val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(d).get)
          outboxManagerActor ! MessageReceiver(device.deviceId, currentStateStr, ConfigKeys.DEVICEOUTBOX)
          d
      }.recover {
        case t: Throwable =>
          log.error(t, s"current AvatarStateRest not available: ${device.deviceId}")
          val d = DeviceStateManager.createNewDeviceState(device, AvatarState(deviceId = device.deviceId, inSync = Some(false), currentDeviceSignature = drdPatched.s))
          val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(d).get)
          outboxManagerActor ! MessageReceiver(device.deviceId, currentStateStr, ConfigKeys.DEVICEOUTBOX)
          d
      }

      if (DeviceManager.checkProperty(device, Const.STOREDATA)) {
        log.debug(s"stores data for ${device.deviceId}")
        deviceStateUpdate.map { d =>
          persistenceActor forward(drdPatched, d)
        }
      }
      else {
        log.debug(s"stores no data for ${device.deviceId}")
        deviceStateUpdate.map(s ! _)
      }


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
    val start = System.currentTimeMillis()
    AvatarStateManagerREST.setReported(restDevice = device, payload, signature) collect {
      case Some(currentAvatarState) =>
        log.debug(s"AvatarStateManagerREST.setReported(${device.deviceId}) took ${System.currentTimeMillis() - start}ms")
        val esStart = System.currentTimeMillis()
        val dsu = DeviceStateManager.createNewDeviceState(device, currentAvatarState)
        DeviceStateManager.upsert(state = dsu)
        log.debug(s"DeviceStateManager.createNewDeviceState(${device.deviceId}) took ${System.currentTimeMillis() - esStart}ms")
        processStateTimer.stop

        Some(dsu)
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