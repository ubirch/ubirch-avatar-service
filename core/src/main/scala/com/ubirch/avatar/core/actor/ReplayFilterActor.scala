package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
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
    .actorSelection(ActorNames.MSG_PROCESSOR)

  val redis = RedisClientUtil.getRedisClient()

  override def receive: Receive = {

    case (drd: DeviceDataRaw, device: Device) =>
      val s = context.sender()

      val currenSig = drd.s.get
      val now = DateTime.now
      val dur = Days.daysBetween(drd.ts, now)
      if (dur.getDays > Config.getMessageMaxAge)
        s ! JsonErrorResponse(
          errorType = "ValidationError",
          errorMessage = "received message from the past error"
        )
      else if (drd.s.isEmpty)
        s ! JsonErrorResponse(
          errorType = "ValidationError",
          errorMessage = "received message without a signature"
        )
      else
        redis.get(currenSig) map {
          case Some(v) =>
            s ! JsonErrorResponse(
              errorType = "ValidationError",
              errorMessage = "replay attack detected"
            )
          case None =>
            DeviceDataRawManager.loadBySignature(drd.s.get).map {
              case Some(d) =>
                s ! JsonErrorResponse(
                  errorType = "ValidationError",
                  errorMessage = "delayed replay attack detected"
                )
              case None =>
                redis.set(currenSig, drd.ts.toString, exSeconds = Some(Config.getMessageSignatureCache))
                processorActor tell((drd, device), sender = s)
            }
        }

  }

  override def unhandled(message: Any): Unit = super.unhandled(message)
}

object ReplayFilterActor {
  def props()(implicit mongo: MongoUtil): Props = Props(new ReplayFilterActor())
}