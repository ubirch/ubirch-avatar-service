package com.ubirch.avatar.backend

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.backend.actor.util.ActorStarter
import com.ubirch.avatar.backend.route.MainRoute
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.util.server.{ElasticsearchMappings, MongoConstraints}
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage
import com.ubirch.util.mongo.connection.MongoUtil
import org.elasticsearch.client.transport.TransportClient

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
  try {
    createElasticsearchMappings()
  }
  catch {
    case e: Exception =>
      logger.error("es startup bug", e)
  }
  ActorStarter.init(system)

  //import io.prometheus.client.hotspot.DefaultExports
  // start default prometheus JVM collectors
  //DefaultExports.initialize()

  DeviceTypeManager.init()

  //  val camel = CamelExtension(system)
  //  val camelContext = camel.context
  //  val registry = camel.context.getComponent("sqs")

  val bindingFuture = start()

  stop()

  private def start(): Future[ServerBinding] = {

    val interface = Config.httpInterface
    val port = Config.httpPort
    val pinterface = Config.httpPrometheusInterface
    val pport = Config.httpPrometheusPort

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    logger.info(s"start prometheus http server on $pinterface:$pport")
    import io.prometheus.client.exporter.HTTPServer
    val server = new HTTPServer(pinterface, pport)

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
