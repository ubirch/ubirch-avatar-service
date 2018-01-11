package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.ubirch.avatar.core.check.DeepCheckManager
import com.ubirch.util.deepCheck.model.{DeepCheckRequest, DeepCheckResponse}
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-08
  */
class DeepCheckActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer) extends Actor
  with ActorLogging {

  private implicit val system = context.system
  private implicit val executionContext = system.dispatcher

  override def receive: Receive = {

    case _: DeepCheckRequest =>
      val sender = context.sender()
      deepCheck() map (sender ! _)

    case _ => log.error("unknown message")

  }

  private def deepCheck(): Future[DeepCheckResponse] = DeepCheckManager.connectivityCheck()

}
