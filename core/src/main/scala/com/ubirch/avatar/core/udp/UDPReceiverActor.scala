package com.ubirch.avatar.core.udp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.HttpExt
import akka.io.{IO, Udp}
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.MessageValidatorActor
import com.ubirch.avatar.core.msgpack.MsgPacker
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 14/09/16.
  */

case class StartTimer()

case class UpdateStats()

case class CountPaket()

case class CountDuplicatePaket()

class UDPReceiverActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer) extends Actor with akka.actor.ActorLogging {

  import context.{dispatcher, system}

  private val validatorActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new MessageValidatorActor())), ActorNames.MSG_VALIDATOR)

  val udpInterface = Config.udpInterface
  val udpPort = Config.udpPort

  var currentRate: Double = 0.0
  var currentDublicateRate: Double = 0.0

  var counter = 0
  var dublicateCounter = 0

  var lastTimestamp = DateTime.now.getMillis

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(udpInterface, udpPort), List(Udp.SO.ReceiveBufferSize(1024 * 1024 * 20)))

  def receive = {
    case Udp.Bound(local) =>
      log.info(s"Bound to: $local")
      context.become(ready(sender()))
      self ! StartTimer
  }

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    log.info("preStart")
  }

  def ready(socket: ActorRef): Receive = {
    case StartTimer =>
      system.scheduler.schedule(
        5 second, 1 second, self, UpdateStats)
      log.info("rate calculation started")
    case UpdateStats =>
      val now = DateTime.now.getMillis
      currentRate = (counter.toDouble / (now - lastTimestamp)) * 1000
      currentDublicateRate = (dublicateCounter.toDouble / (now - lastTimestamp)) * 1000
      counter = 0
      dublicateCounter = 0
      lastTimestamp = now
      if (currentRate > 0.001 || currentDublicateRate > 0.001) {
        log.info(s">>> current rate: ${currentRate.formatted("%.2f")} msgs/s")
        log.info(s">>> current duplicate rate: ${currentDublicateRate.formatted("%.2f")} msgs/s")
      }
    case CountPaket =>
      counter += 1
    case CountDuplicatePaket =>
      dublicateCounter += 1

    case Udp.Received(data, remote) =>
      val bytes = data.toByteBuffer.array()
      log.debug(s"received from: $remote data: $data")
      val cData = MsgPacker.unpackCalliope(data.toArray)
      log.debug(s"calliope data. $cData")

      cData.foreach { cd =>
        val drd = DeviceDataRaw(
          v = MessageVersion.v000,
          a = HashUtil.sha512Base64(cd.deviceId.toString),
          did = Some(cd.deviceId.toString),
          p = cd.payload
        )
        validatorActor ! drd
      }

    case Udp.Unbind =>
      log.info("Unbind")
      socket ! Udp.Unbind
    case Udp.Unbound =>
      log.info("Unbound")
      context.stop(self)

    case jer: JsonErrorResponse =>
    case dsu: DeviceStateUpdate =>
    case _ =>
      log.error("received unknown message")
  }
}