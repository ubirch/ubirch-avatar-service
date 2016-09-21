package com.ubirch.avatar.backend

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.avatar.backend.route.MainRoute
import com.ubirch.avatar.config.Config

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object Boot extends App with LazyLogging {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  logger.info("ubirchAvatarService started")

  implicit val timeout = Timeout(15 seconds)

  val bindingFuture = start()

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() = {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    }
  })



  def start(): Future[ServerBinding] = {

    val interface = Config.interface
    val port = Config.port
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    Http().bindAndHandle((new MainRoute).myRoute, interface, port)

  }

}
