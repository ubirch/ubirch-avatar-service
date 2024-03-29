package com.ubirch.avatar.core.actor

import akka.actor.{ Actor, ActorLogging }
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.ubirch.avatar.core.check.{ DeepCheckManager, LiveCheckManager, ReadyCheckManager }
import com.ubirch.util.deepCheck.model.ReadyCheckRequest
import com.ubirch.util.deepCheck.model.LiveCheckRequest
import com.ubirch.util.deepCheck.model.DeepCheckRequest
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil

import scala.util.{ Failure, Success }

/**
  * author: cvandrei
  * since: 2017-06-08
  */
class ServcieCheckActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer)
  extends Actor
  with ActorLogging {

  implicit private val system = context.system
  implicit private val executionContext = system.dispatcher

  override def receive: Receive = {

    case _: DeepCheckRequest =>
      val sender = context.sender()
      DeepCheckManager.connectivityCheck().onComplete {
        case Success(dcr) =>
          sender ! dcr
        case Failure(t) =>
          sender ! JsonErrorResponse(
            errorType = "InternalError",
            errorMessage = t.getMessage
          )
      }

    case _: ReadyCheckRequest =>
      val sender = context.sender()
      ReadyCheckManager.connectivityCheck().onComplete {
        case Success(dcr) =>
          sender ! dcr
        case Failure(t) =>
          sender ! JsonErrorResponse(
            errorType = "InternalError",
            errorMessage = t.getMessage
          )
      }

    case req: LiveCheckRequest =>
      val sender = context.sender()
      LiveCheckManager.connectivityCheck().onComplete {
        case Success(dcr) =>
          sender ! dcr
        case Failure(t) =>
          sender ! JsonErrorResponse(
            errorType = "InternalError",
            errorMessage = t.getMessage
          )
      }

    case _ => log.error("unknown message")

  }

}
