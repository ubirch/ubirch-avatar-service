package com.ubirch.avatar.backend

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.backend.route.MainRoute
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.util.server.ElasticsearchMappings
import com.ubirch.transformer.TransformerManager
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage
import com.ubirch.util.mongo.connection.MongoUtil

import org.elasticsearch.client.transport.TransportClient

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object Boot extends App
  with ElasticsearchMappings
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val ws: WSClient = NingWSClient()

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  logger.info("ubirchAvatarService started")

  implicit val timeout = Timeout(Config.actorTimeout seconds)

  implicit val esClient: TransportClient = ESSimpleStorage.getCurrentEsClient
  createElasticsearchMappings()

  val bindingFuture = start()

  TransformerManager.init()
  DeviceTypeManager.init()

  stop()


  private def start(): Future[ServerBinding] = {

    val interface = Config.interface
    val port = Config.port
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    logger.info(s"start http server on $interface:$port")

    Http().bindAndHandle((new MainRoute).myRoute, interface, port)

  }

  private def stop() = {

    Runtime.getRuntime.addShutdownHook(new Thread() {

      override def run(): Unit = {

        bindingFuture.flatMap(_.unbind()).onComplete {

          case Success(_) =>
            system.terminate()
            esClient.close()
            ws.close()
            mongo.close()

          case Failure(f) =>
            logger.error("shutdown failed", f)
            system.terminate()
            esClient.close()
            ws.close()
            mongo.close()

        }

      }

    })

  }

}
