package com.ubirch.avatar.core.actor

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.notary.client.NotaryClient
import com.ubirch.util.json.Json4sUtil

/**
  * Created by derMicha on 28/10/16.
  */
class MessageNotaryActor extends Actor with StrictLogging {

  override def receive: Receive = {
    case drd: DeviceDataRaw =>
      val s = sender
      logger.debug(s"received message: $drd")

    //      val payloadStr = Json4sUtil.jvalue2String(drd.p)
    //
    //      NotaryClient.notarize(
    //        blockHash = "payloadStr",
    //        dataIsHash = false
    //      ) match {
    //        case Some(resp) =>
    //          logger.debug(s"btx hash for message ${drd.id} is ${resp.hash}")
    //        case None =>
    //          logger.error(s"notarize failed for: $drd")
    //      }

    case _ =>
      logger.error("received unknown message")
  }

}
