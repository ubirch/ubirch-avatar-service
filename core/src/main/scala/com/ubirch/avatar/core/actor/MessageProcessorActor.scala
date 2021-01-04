package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.ubirch.avatar.config.{Config, Const}
import com.ubirch.avatar.core.avatar.AvatarStateManagerREST
import com.ubirch.avatar.core.device.{DeviceManager, DeviceStateManager}
import com.ubirch.avatar.core.prometheus.Timer
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{AvatarState, DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.json4s.{JValue, MappingException}

import scala.concurrent.duration._
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

  private val outboxManagerActor = context.actorSelection(ActorNames.DEVICE_OUTBOX_MANAGER_PATH)

  private val processStateTimer = new Timer(s"process_state_${scala.util.Random.nextInt(100000)}")

  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)


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

      val persistedAndForwarded =
        if (DeviceManager.checkProperty(device, Const.STOREDATA)) {
          log.debug(s"stores data for ${device.deviceId}")
          persistenceActor ? drdPatched map {
            case boolean if boolean == true =>
              // publish incoming raw data to trackle and return true or false depending on success
              outboxManagerActor ? (device, drdPatched)
            case false => false
            case unknown =>
              log.error(s"received unknown message type $unknown")
              false
          }
        } else {
          log.debug(s"stores no data for ${device.deviceId}")
          Future.successful(true)
        }

      persistedAndForwarded.map {
        case false => s ! JsonErrorResponse(errorType = "database error",
          errorMessage = "something went wrong storing or forwarding the deviceDataRaw in database.")
        case true =>
          processPayload(device, pl, drdPatched.s).map {
            case Some(d: DeviceStateUpdate) =>
              log.debug(s"current AvatarState updated: ${device.deviceId}")
              //              val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(d).get)
              //              outboxManagerActor ! MessageReceiver(device.deviceId, currentStateStr, ConfigKeys.DEVICEOUTBOX)
              s ! d
            case None =>
              log.error(s"current AvatarStateRest not available: ${device.deviceId}")
              val d = DeviceStateManager.createNewDeviceState(AvatarState(deviceId = device.deviceId, inSync = Some(false), currentDeviceSignature = drdPatched.s))
              s ! d
          }.recover {
            case t: Throwable =>
              log.error(t, s"current AvatarStateRest not available: ${device.deviceId}")
              //              val currentStateStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(d).get)
              //              outboxManagerActor ! MessageReceiver(device.deviceId, currentStateStr, ConfigKeys.DEVICEOUTBOX)
              val d = DeviceStateManager.createNewDeviceState(AvatarState(deviceId = device.deviceId, inSync = Some(false), currentDeviceSignature = drdPatched.s))
              s ! d
          }

      }
  }

  private def processPayload(device: Device, payload: JValue, signature: Option[String] = None): Future[Option[DeviceStateUpdate]] = {
    processStateTimer.start
    val start = System.currentTimeMillis()
    AvatarStateManagerREST.setReported(restDevice = device, payload, signature) collect {
      case Some(currentAvatarState) =>
        log.debug(s"AvatarStateManagerREST.setReported(${device.deviceId}) took ${System.currentTimeMillis() - start}ms")
        val esStart = System.currentTimeMillis()
        val dsu = DeviceStateManager.createNewDeviceState(currentAvatarState)
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