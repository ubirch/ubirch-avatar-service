package com.ubirch.avatar.core.actor

import java.io.ByteArrayInputStream

import akka.actor.{Actor, ActorLogging}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil
import org.apache.commons.codec.binary.Hex
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.ValueType

import scala.collection.mutable
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

      val hexVal = Hex.encodeHexString(binData)
      log.info(s"got some MsgPack data: $hexVal")

      val (did, ts) = unpack(binData)

      log.info(s"deviceId: $did / t: $ts")

      s ! "OK: Thanks!"
    case _ =>
      log.error("received unknown message")
      sender ! "NOK: received unknown message"
  }

  private def unpack(binData: Array[Byte]) = {

    val ids: mutable.ArrayBuffer[Long] = mutable.ArrayBuffer.empty
    val temps: mutable.ArrayBuffer[Int] = mutable.ArrayBuffer.empty

    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator()
    var done = false
    while (itr.hasNext && !done) {
      val v = itr.next()
      v.getType match {
        case ValueType.INTEGER =>
          val value = v.asIntegerValue.getLong
          ids.append(value)
        case ValueType.ARRAY =>
          println(v.getType)
          val arr = v.asArrayValue()
          val itr2 = arr.iterator()
          while (itr2.hasNext) {
            val tval = itr2.next().asIntegerValue().getInt
            temps.append(tval)
          }
          done = true
        case _ =>
      }
    }
    val did = ids.mkString("-")
    val ts = temps.toSeq
    (did, ts)
  }
}
