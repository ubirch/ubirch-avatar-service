package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.core.msgpack.MsgPacker
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.apache.commons.codec.binary.Hex

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by derMicha on 18/07/17.
  */
class MessageMsgPackProcessorActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer)
  extends Actor
    with MyJsonProtocol


    with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  private val validatorActor = context.system.actorOf(Props(new MessageValidatorActor()))
  //private val validatorActor = context.system.actorSelection(ActorNames.MSG_VALIDATOR)

  override def receive: Receive = {
    case binData: Array[Byte] =>
      val s = sender()

      val hexVal = Hex.encodeHexString(binData)
      log.info(s"got some MsgPack data: $hexVal")
      try {
        val (u, t) = MsgPacker.unpack(binData)
        DeviceDataRawManager.create(did = u, vals = t, mpraw = binData) match {
          case Some(drd) =>
            validatorActor forward drd
          case None =>
            log.error("could not parse input msgpack data")
            s ! JsonErrorResponse(errorType = "Validation Error", errorMessage = "Invalid Data")
        }
      }
      catch {
        case e: Exception =>
          log.error("received invalid data", e)
          sender ! JsonErrorResponse(errorType = "Invalid Data Error", errorMessage = "Incalid Dataformat")
      }
    case _ =>
      log.error("received unknown message")
      sender ! JsonErrorResponse(errorType = "Validation Error", errorMessage = "Invalid Input Data")
  }

}
