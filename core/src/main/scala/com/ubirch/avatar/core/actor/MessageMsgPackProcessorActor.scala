package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.ubirch.avatar.core.msgpack.MsgPacker
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.crypto.hash.HashUtil
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

        //val (u, t) = MsgPacker.unpackTrackle(binData)

        val cData = MsgPacker.unpackSingleValue(binData.toArray)
        log.debug(s"calliope data. $cData")
        cData foreach { cd =>
          val hwDeviceId = cd.deviceId.toString.toLowerCase()
          val drd = DeviceDataRaw(
            v = if (cd.signature.isDefined) MessageVersion.v002 else MessageVersion.v000,
            a = HashUtil.sha512Base64(hwDeviceId.toLowerCase),
            did = Some(cd.deviceId.toString),
            mpraw = Some(hexVal),
            p = cd.payload,
            //k = Some(Base64.getEncoder.encodeToString(Hex.decodeHex("80061e8dff92cde5b87116837d9a1b971316371665f71d8133e0ca7ad8f1826a".toCharArray))),
            s = cd.signature
          )

          //        DeviceDataRawManager.create(did = u, vals = t, mpraw = binData) match {
          //          case Some(drd) =>
          //            validatorActor forward drd
          //          case None =>
          //            log.error("could not parse input msgpack data")
          //            s ! JsonErrorResponse(errorType = "Validation Error", errorMessage = "Invalid Data")
          //        }
          validatorActor forward drd
        }
      }
      catch {
        case e: Exception =>
          log.error("received invalid data", e)
          sender ! JsonErrorResponse(errorType = "Invalid Data Error", errorMessage = "Incalid Dataformat")
      }
    case _ =>
      log.error("received unknown msgPack message ")
      sender ! JsonErrorResponse(errorType = "Validation Error", errorMessage = "Invalid Input Data")
  }

}
