package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.camel.CamelMessage
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.actor.TransformerProducerActor
import com.ubirch.transformer.model.MessageReceiver
import com.ubirch.util.json.Json4sUtil
import org.apache.camel.Message

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

/**
  * Created by derMicha on 24/05/17.
  */

class DeviceOutboxManagerActor extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  final val TRACTOR_BASE: String = "AVS-transformerproducer-actor-api-"

  final val TRACTOR_BASE_PATH: String = s"/user/$TRACTOR_BASE"

  final val DOMACTOR_BASE: String = "AVS-deviceoutboxmanager-actor-api-"

  final val DOMACTOR_BASE_PATH: String = s"/user/$DOMACTOR_BASE"

  override def receive: Receive = {

    case (device: Device, drd: DeviceDataRaw) =>
      val drdExt = drd.copy(deviceId = Some(device.deviceId))
      device.pubRawQueues.getOrElse(Set()).foreach { queue =>

        getSqsProducer(queue).map { taRef =>
          Json4sUtil.any2String(drdExt) match {
            case Some(drdStr) =>
              taRef ! drdStr
            case None =>
              log.error(s"error sending for device ${device.deviceId} raw message ${drd.id}")
          }
        }
      }

    case mr: MessageReceiver =>

      getMqttProducer(mr).map { taRef =>
        taRef ! mr.message
      }
  }

  override def unhandled(message: Any): Unit = {
    if (message.isInstanceOf[Message]) {
      //log.error(s"received unknown message body: ${message.asInstanceOf[Message].getBody.toString}")
    }
    else if (message.isInstanceOf[CamelMessage]) {
      //log.error(s"received unknown message body: ${message.asInstanceOf[Message].getBody.toString}")
    }
    else
      log.error(s"${message.getClass.toString}")
  }


  private def getSqsProducer(queue: String): Future[ActorRef] = {

    val curRefBase = s"$TRACTOR_BASE${queue}"
    val curRefBasePath = s"$TRACTOR_BASE_PATH${queue}"

    val aref = context.system.actorSelection(curRefBasePath)
    val fs: FiniteDuration = 100 millis

    aref.resolveOne(fs).map { ar =>
      log.debug(s"reused actor with path: $curRefBasePath")
      ar
    }.recover {
      case t =>
        log.debug(s"had to create fresh actor with path: $curRefBasePath")
        val acr = context.system.actorOf(TransformerProducerActor.props(queue), curRefBase)
        acr
    }
  }

  private def getMqttProducer(mr: MessageReceiver): Future[ActorRef] = {

    val curRefBase = s"$DOMACTOR_BASE${mr.getKey}"
    val curRefBasePath = s"$DOMACTOR_BASE_PATH${mr.getKey}"

    val aref = context.system.actorSelection(curRefBasePath)
    val fs: FiniteDuration = 500 millis

    aref.resolveOne(fs).map { ar =>
      log.debug(s"reused actor with path: $curRefBasePath")
      ar
    }.recover {
      case t: Throwable =>
        log.debug(s"had to create fresh actor with path: $curRefBasePath")
        context.system.actorOf(DeviceStateUpdateActor.props(mr.topic), curRefBase)
    }
  }

}

object DeviceOutboxManagerActor {
  def props(): Props = Props(new DeviceOutboxManagerActor())
}

