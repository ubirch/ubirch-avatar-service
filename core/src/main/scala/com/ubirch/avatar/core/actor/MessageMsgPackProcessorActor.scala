package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil
import org.velvia.msgpack._

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 18/07/17.
  */
class MessageMsgPackProcessorActor(implicit mongo: MongoUtil)
  extends Actor
    with MyJsonProtocol


    with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  //  private val validatorActor = context.system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new MessageValidatorActor())), ActorNames.MSG_VALIDATOR)
  private val validatorActor = context.system.actorSelection(ActorNames.MSG_VALIDATOR)

  override def receive: Receive = {
    case binData: Array[Byte] =>
      val s = sender()

      import org.json4s._
      import org.velvia.msgpack.Json4sCodecs._

      val ddrJson = unpack[JValue](binData)
      ddrJson.extractOpt[DeviceDataRaw] match {
        case Some(ddr) =>
          validatorActor forward ddr
        case _ =>
          s ! "NOK: received invalid message"
      }
    case _ =>
      log.error("received unknown message")
      sender ! "NOK: received unknown message"
  }

}
