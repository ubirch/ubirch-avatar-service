package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.camel.CamelMessage
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.transformer.actor.TransformerProducerActor2.{KafkaMessage, PublisherException, PublisherSuccess}
import com.ubirch.transformer.actor.{TransformerProducerActor, TransformerProducerActor2}
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

  private val transformerProducerActor2 = context.actorSelection(ActorNames.TRANSFORMER_PRODUCER2_PATH)

  override def receive: Receive = {

    case (device: Device, drd: DeviceDataRaw) =>
      val s = sender()
      val drdExt = if (drd.mppay.isDefined && drd.mppay.get.length > 50000) {
        drd.copy(
          deviceId = Some(device.deviceId),
          mppay = None,
          mpraw = None
        )
      }
      else
        drd.copy(deviceId = Some(device.deviceId))

      Json4sUtil.any2String(drdExt) match {
        case Some(drdStr) =>
          transformerProducerActor2 ! KafkaMessage(drdStr)
          s ! true
        case None =>
          log.error(s"error sending for device ${device.deviceId} raw message ${drd.id}")
          s ! false
      }
      /*
      Future.sequence(device.pubRawQueues
        .getOrElse(Set())
        .map { queue =>
          // @TODO this part should be changed as Kafka interface
          getSqsProducer(queue).map { taRef =>
            Json4sUtil.any2String(drdExt) match {
              case Some(drdStr) =>
                taRef ! drdStr
                true
              case None =>
                log.error(s"error sending for device ${device.deviceId} raw message ${drd.id}")
                false
            }
          }
        }).map(s ! !_.contains(false))*/

    //    case mr: MessageReceiver =>

    //      getMqttProducer(mr).map { taRef =>
    //        taRef ! mr.message
    //      }

    case PublisherSuccess(_) =>
      log.info("succeeded to send payload.")

    case PublisherException(err) =>
      log.error(s"failed to send payload. err: ${err.getMessage}")

    case Terminated(actorRef) =>
      log.warning("Actor {} terminated", actorRef)
  }

  override def unhandled(message: Any): Unit = {
    message match {
      case _: Message =>
      //log.error(s"received unknown message body: ${message.asInstanceOf[Message].getBody.toString}")
      case _: CamelMessage =>
      //log.error(s"received unknown message body: ${message.asInstanceOf[Message].getBody.toString}")
      case _ => log.error(s"${message.getClass.toString}")
    }
  }


  def transformerProducerActor(queue: String, curRefBase: String): ActorRef = synchronized {
    log.debug("Creating actor := {}", curRefBase)
    val actorRef = context.system.actorOf(TransformerProducerActor.props(queue), curRefBase)
    context.watch(actorRef)
  }

  //@TODO refactor
  private def getSqsProducer(queue: String): Future[ActorRef] = {

    val curRefBase = s"$TRACTOR_BASE$queue"
    val curRefBasePath = s"$TRACTOR_BASE_PATH$queue"

    val aref = context.system.actorSelection(curRefBasePath)

    def getActor = aref.resolveOne(timeout = 100 millis)

    getActor.map { ar =>
      log.debug(s"reused sqs producer actor with path: $curRefBasePath")
      ar
    }.recover {
      case e =>
        log.debug("1. exception={} message={}", e.getClass.getCanonicalName, e.getMessage)
        transformerProducerActor(queue, curRefBase)
    }.recoverWith {
      case e =>
        log.debug("2. exception={} message={}", e.getClass.getCanonicalName, e.getMessage)
        getActor
    }.recover {
      case e =>
        log.error("3. exception={} message={}", e.getClass.getCanonicalName, e.getMessage)
        throw new Exception("Error Creating Transformer Actor", e)
    }

  }

  //  //@TODO refactor
  //  private def getMqttProducer(mr: MessageReceiver): Future[ActorRef] = {
  //
  //    val curRefBase = s"$DOMACTOR_BASE${mr.getKey}"
  //    val curRefBasePath = s"$DOMACTOR_BASE_PATH${mr.getKey}"
  //
  //    val aref = context.system.actorSelection(curRefBasePath)
  //    val fs: FiniteDuration = 500 millis
  //
  //    aref.resolveOne(fs).map { ar =>
  //      log.debug(s"reused mqtt producer actor with path: $curRefBasePath")
  //      ar
  //    }.recover {
  //      case t: Throwable =>
  //        log.debug(s"had to create fresh actor with path: $curRefBasePath")
  //        context.system.actorOf(DeviceStateUpdateActor.props(mr.topic), curRefBase)
  //    }
  //  }

}

object DeviceOutboxManagerActor {
  def props(): Props = Props(new DeviceOutboxManagerActor())
}

