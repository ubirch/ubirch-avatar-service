package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incoming messages
  */
class MessageDeviceCheckActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  private val validatorActor = context.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new MessageValidatorActor())), ActorNames.MSG_PROCESSOR)

  override def receive: Receive = {

    case drd: DeviceDataRaw =>
      val s = sender()
      validatorActor forward drd
      log.debug(s"received message version: ${drd.v}")

    //      DeviceManager.infoByHashedHwId(drd.a).map {
    //        case Some(dev) =>
    //          validatorActor forward data
    //        case None =>
    //          s ! logAndCreateErrorResponse(s"invalid hwDeviceId: ${drd.a}", "ValidationError")
    //      }

    case _ =>
      sender ! logAndCreateErrorResponse("received unknown message", "ValidationError")
  }

  private def logAndCreateErrorResponse(msg: String, errType: String): JsonErrorResponse = {
    log.error(msg)
    JsonErrorResponse(errorType = errType, errorMessage = msg)
  }

}
