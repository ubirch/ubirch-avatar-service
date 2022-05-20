package com.ubirch.avatar.core.actor

import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.msgpack.MsgPackPacker
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.apache.commons.codec.binary.Hex

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 18/07/17.
  */
class MessageMsgPackProcessorActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer)
  extends Actor
  with MyJsonProtocol
  with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val validatorActor = context.actorSelection(ActorNames.MSG_VALIDATOR_PATH)

  override def receive: Receive = {

    case binData: Array[Byte] =>
      val s = sender()
      try {
        val hexVal = Hex.encodeHexString(binData)
        log.debug(s"got some msgPack data: $hexVal")
        MsgPackPacker.processUbirchProt(binData) match {
          case ddr: DeviceDataRaw =>
            log.info(s"validating msgpack data (ddr): $ddr")
            validatorActor forward ddr
          case _ =>
            s ! JsonErrorResponse(errorType = "validation error", errorMessage = "invalid bin data")
        }
      } catch {
        case e: Exception =>
          log.error(e, s"received invalid data")
          s ! JsonErrorResponse(
            errorType = "ValidationError",
            errorMessage = s"invalid dataformat ${e.getMessage}")
      }

    case _ =>
      log.error("received unknown msgPack message ")
      sender() ! JsonErrorResponse(errorType = "Validation Error", errorMessage = "invalid input data")
  }

}

object MessageMsgPackProcessorActor {
  def props()(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer): Props =
    new RoundRobinPool(Config.akkaNumberOfBackendWorkers)
      .props(Props(new MessageMsgPackProcessorActor()))
}
