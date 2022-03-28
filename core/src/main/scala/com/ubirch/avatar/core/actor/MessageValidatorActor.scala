package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incoming messages
  */
class MessageValidatorActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  implicit val actorSystem: ActorSystem = context.system
  private val processorActor = context
    .actorSelection(ActorNames.MSG_PROCESSOR_PATH)


  override def receive: Receive = {

    case drd: DeviceDataRaw if drd.v == MessageVersion.v000 =>
      //This seems to be the Trackle default message version
      val s = sender()

      log.debug(s"received message with version ${drd.v}")

      DeviceCoreUtil.validateSimpleMessage(hashedHwDeviceId = drd.a).map {
        case Some(dev) =>
          processorActor tell((drd, dev), sender = s)
        case None =>
          s ! logAndCreateErrorResponse(errType = "ValidationError", msg = s"invalid hwDeviceId: ${drd.a}", deviceId = None, hashedHwDeviceId = Some(drd.a))
      }.recover {
        case t: Throwable =>
          log.error(t, "unable to validate message")
          s ! logAndCreateErrorResponse(errType = "ValidationError", msg = s"unknown error: ${t.getMessage}", deviceId = None, hashedHwDeviceId = Some(drd.a))
      }

    case drd: DeviceDataRaw =>
      sender ! logAndCreateErrorResponse(s"received unknown message version: ${drd.v}", "ValidationError", deviceId = None, hashedHwDeviceId = Some(drd.a))
    case _ =>
      sender ! logAndCreateErrorResponse(msg = "received unknown message", errType = "ValidationError", deviceId = None, hashedHwDeviceId = None)
  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received unknown message: ${message.toString} (${message.getClass.toGenericString}) from: ${context.sender()}")
  }


  private def logAndCreateErrorResponse(msg: String, errType: String, deviceId: Option[String], hashedHwDeviceId: Option[String]): JsonErrorResponse = {
    log.error(msg)
    val jer = JsonErrorResponse(errorType = errType, errorMessage = msg)
    //    if (deviceId.isDefined)
    //      outboxManagerActor ! MessageReceiver(deviceId.get, jer.toJsonString, ConfigKeys.DEVICEOUTBOX)
    jer
  }

}

object MessageValidatorActor {
  def props()(implicit mongo: MongoUtil,
              httpClient: HttpExt,
              materializer: Materializer): Props = new RoundRobinPool(
    Config.akkaNumberOfFrontendWorkers).props(
    Props(new MessageValidatorActor()))
}