package com.ubirch.avatar.backend

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.backend.route.MainRoute
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.util.server.{ElasticsearchMappings, MongoConstraints}
import com.ubirch.transformer.TransformerManager
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage
import com.ubirch.util.mongo.connection.MongoUtil
import org.elasticsearch.client.transport.TransportClient
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.ubirch.avatar.core.udp.UDPReceiverActor

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
  with MongoConstraints
  with StrictLogging {

  implicit val system = ActorSystem("AvatarService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val httpClient: HttpExt = Http()

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)
  createMongoConstraints()

  logger.info("ubirchAvatarService started")

  implicit val timeout = Timeout(Config.actorTimeout seconds)

  implicit val esClient: TransportClient = ESSimpleStorage.getCurrentEsClient
  createElasticsearchMappings()

  //private val udpReceiverActor = system.actorOf(Props(new UDPReceiverActor))

  //  val camel = CamelExtension(system)
  //  val camelContext = camel.context
  //  val registry = camel.context.getComponent("sqs")

  val bindingFuture = start()

  TransformerManager.init(system)
  DeviceTypeManager.init()

  stop()

  private def start(): Future[ServerBinding] = {

    val interface = Config.httpInterface
    val port = Config.httpPort
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
            mongo.close()

          case Failure(f) =>
            logger.error("shutdown failed", f)
            system.terminate()
            esClient.close()
            mongo.close()

        }

      }

    })

  }

}
