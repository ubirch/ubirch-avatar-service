package com.ubirch.transformer.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.core.actor.DeviceMessageProcessedActor
import com.ubirch.transformer.model.MessageReceiver

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

/**
  * Created by derMicha on 24/05/17.
  */

class TransformerOutboxManagerActor extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  final val TRACTOR_BASE: String = "AVS-transformeroutproducer-actor-"

  final val TRACTOR_BASE_PATH: String = s"/user/$TRACTOR_BASE"

  final val DMACTOR_BASE: String = "AVS-devicemessageprocessed-actor-"

  final val DMACTOR_BASE_PATH: String = s"/user/$DMACTOR_BASE"

  override def receive: Receive = {

    case mr: MessageReceiver =>

      mr.target match {
        case ConfigKeys.INTERNOUTBOX =>
          getInternProducer(mr).map(_ ! mr.message)
        case ConfigKeys.EXTERNOUTBOX =>
          getExternProducer(mr).map(_ ! mr.message)
        case _ =>
          log.error(s"invalid target: ${mr.target}")
      }
  }

  //@TODO refactor
  private def getInternProducer(mr: MessageReceiver): Future[ActorRef] = {

    val curRefBase = s"$TRACTOR_BASE${mr.topic}"
    val curRefBasePath = s"$TRACTOR_BASE_PATH${mr.topic}"

    val aref = context.actorSelection(curRefBasePath)
    val fs: FiniteDuration = 100 millis

    aref.resolveOne(fs).map { ar =>
      log.debug(s"reused actor with path: $curRefBasePath")
      ar
    }.recover {
      case t =>
        log.debug(s"had to create fresh actor with path: $curRefBasePath")
        val acr = context.system.actorOf(TransformerOutProducerActor.props(mr.topic), curRefBase)
        acr
    }
  }

  //@TODO refactor
  private def getExternProducer(mr: MessageReceiver): Future[ActorRef] = {

    val curRefBase = s"$DMACTOR_BASE${mr.topic}"
    val curRefBasePath = s"$DMACTOR_BASE_PATH${mr.topic}"

    val aref = context.system.actorSelection(curRefBasePath)
    val fs: FiniteDuration = 100 millis

    aref.resolveOne(fs).map { ar =>
      log.debug(s"reused actor with path: $curRefBasePath")
      ar
    }.recover {
      case t =>
        log.debug(s"had to create fresh actor with path: $curRefBasePath")
        val acr = context.system.actorOf(DeviceMessageProcessedActor.props(mr.topic), curRefBase)
        acr
    }
  }

  override def unhandled(message: Any): Unit = {

  }
}
