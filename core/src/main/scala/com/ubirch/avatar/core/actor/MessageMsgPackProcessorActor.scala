package com.ubirch.avatar.core.actor

import java.util.Base64

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.mongo.connection.MongoUtil
import org.json4s.JValue
import org.velvia.msgpack._

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 18/07/17.
  */
class MessageMsgPackProcessorActor(implicit mongo: MongoUtil) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  //  private val validatorActor = context.system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new MessageValidatorActor())), ActorNames.MSG_VALIDATOR)
  private val validatorActor = context.system.actorSelection(ActorNames.MSG_VALIDATOR)

  override def receive: Receive = {
    case u64Message: String =>

      import org.json4s._
      import org.json4s.native.JsonMethods._
      import org.velvia.msgpack.Json4sCodecs._

      val binData = Base64.getDecoder.decode(u64Message)
      val jMap = unpack[JValue](binData)

      log.debug(s"$jMap")

      sender ! s"$jMap"
    case _ =>
      log.error("received unknown message")
      sender ! "NOK: received unknown message"
  }

}
