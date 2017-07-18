package com.ubirch.avatar.backend.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.actor.MessageMsgPackProcessorActor
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}


/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceUpdateMsgPackRoute(implicit mongo: MongoUtil)
  extends ResponseUtil
    with Directives
    with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val msgPackProcessorActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new MessageMsgPackProcessorActor())), ActorNames.MSG_MSGPACK_PROCESSOR)

  val route: Route = {

    path(update / mpack) {

      pathEnd {

        post {

          entity(as[String]) { b64Data =>
            onComplete(msgPackProcessorActor ? b64Data) {
              case Success(resp) =>
                resp match {
                  case result: String =>
                    complete(s"got: $result")
                  case _ =>
                    complete(s"ERROR 1")
                }

              case Failure(t) =>
                complete(s"ERROR 2")
            }


          }
        }
      }
    }
  }
}
