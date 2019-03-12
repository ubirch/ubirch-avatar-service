package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.util.actor.ActorNames
import org.joda.time.DateTime

import scala.concurrent.ExecutionContextExecutor

case class DeviceRawDataReprocessing(day: DateTime)

class DeviceRawDataReprocessingActor extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  private val validatorActor = context.actorSelection(ActorNames.MSG_VALIDATOR_PATH)

  override def receive: Receive = {
    case drdr: DeviceRawDataReprocessing =>
      val day = drdr.day
      DeviceDataRawManager.history(day, 0, 1000).map { datas =>
        datas.filter(_.mpraw.isDefined).foreach(validatorActor ! _)
      }
  }
}

object DeviceRawDataReprocessingActor {
  def props: Props = new RoundRobinPool(Config.akkaNumberOfBackendWorkers).props(Props[DeviceRawDataReprocessingActor])
}
