package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.ubirch.avatar.config.{Config, ConfigKeys, Const}
import com.ubirch.avatar.core.device.{DeviceDataRawManager, DeviceManager}
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.model.MessageReceiver
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.redis.RedisClientUtil
import org.joda.time.{DateTime, Days}

class ReplayFilterActor(implicit mongo: MongoUtil)
  extends Actor
    with ActorLogging {

  implicit val system = context.system
  implicit val disp = context.dispatcher

  private val processorActor = context
    .actorSelection(ActorNames.MSG_PROCESSOR_PATH)

  private val outboxManagerActor = context
    .actorSelection(ActorNames.DEVICE_OUTBOX_MANAGER_PATH)

  val redis = RedisClientUtil.getRedisClient()

  override def receive: Receive = {

    case (drd: DeviceDataRaw, device: Device) =>
      val s = context.sender()

      if (DeviceManager.checkProperty(device, Const.CHECKREPLAY)) {
        val currenSig = drd.s.get
        val now = DateTime.now
        val dur = Days.daysBetween(drd.ts, now)
        if (dur.getDays > Config.getMessageMaxAge) {
          val jer = JsonErrorResponse(
            errorType = "ValidationError",
            errorMessage = s"received message from the past error for device ${device.deviceId}"
          )
          outboxManagerActor ! MessageReceiver(device.deviceId, jer.toJsonString, ConfigKeys.DEVICEOUTBOX)
          s ! jer
        }
        else if (drd.s.isEmpty) {
          val jer = JsonErrorResponse(
            errorType = "ValidationError",
            errorMessage = s"received message without a signature for device ${device.deviceId}"
          )
          outboxManagerActor ! MessageReceiver(device.deviceId, jer.toJsonString, ConfigKeys.DEVICEOUTBOX)
          s ! jer
        }
        else
          redis.get(currenSig) map {
            case Some(v) =>
              val jer = JsonErrorResponse(
                errorType = "ValidationError",
                errorMessage = s"replay attack detected for device ${device.deviceId}"
              )
              outboxManagerActor ! MessageReceiver(device.deviceId, jer.toJsonString, ConfigKeys.DEVICEOUTBOX)
              s ! jer
            case None =>
              DeviceDataRawManager.loadBySignature(drd.s.get).map {
                case Some(d) =>
                  val jer = JsonErrorResponse(
                    errorType = "ValidationError",
                    errorMessage = s"delayed replay attack detected for device ${device.deviceId}"
                  )
                  outboxManagerActor ! MessageReceiver(device.deviceId, jer.toJsonString, ConfigKeys.DEVICEOUTBOX)
                  s ! jer
                case None =>
                  redis.set(currenSig, drd.ts.toString, exSeconds = Some(Config.getMessageSignatureCache))
                  processorActor tell((drd, device), sender = s)
              }
          }
      }
      else {
        processorActor tell((drd, device), sender = s)
      }
  }

  override def unhandled(message: Any): Unit = super.unhandled(message)
}

object ReplayFilterActor {
  def props()(implicit mongo: MongoUtil): Props = Props(new ReplayFilterActor())
}