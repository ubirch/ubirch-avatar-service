package com.ubirch.avatar.backend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.backend.actor.util.ActorStarter
import com.ubirch.avatar.backend.route.MainRoute
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.kafka.EndOfLifeConsumer
import com.ubirch.avatar.util.server.{ElasticsearchMappings, MongoConstraints}
import com.ubirch.server.util.ServerKeys
import com.ubirch.util.elasticsearch.{EsBulkClient, EsSimpleClient}
import com.ubirch.util.mongo.connection.MongoUtil

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object Boot extends App with ElasticsearchMappings with MongoConstraints with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("AvatarService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val httpClient: HttpExt = Http()

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)
  try {
    prepareMongoConstraints()
  } catch {
    case e: Exception =>
      logger.error("mongo startup bug", e)
  }
  logger.info("ubirchAvatarService started")

  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  val publicKey = ServerKeys.pubKeyB64
  logger.info(s"publicKey=$publicKey")

  try {
    createElasticsearchMappings()
  } catch {
    case e: Exception =>
      logger.error("es startup bug", e)
  }
  ActorStarter.init(system)

  val bindingFuture = start()
  private val eolConsumer = new EndOfLifeConsumer(system).runWithRetry(Config.kafkaRetryConfig)

  stop()

  private def start(): Future[ServerBinding] = {

    val interface = Config.httpInterface
    val port = Config.httpPort

    implicit val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)

    logger.info(s"start http server on $interface:$port")

    Http().bindAndHandle((new MainRoute).myRoute, interface, port)

  }

  private def stop(): Unit = {

    Runtime.getRuntime.addShutdownHook(new Thread() {

      override def run(): Unit = {

        bindingFuture.flatMap(_.unbind()).onComplete {

          case Success(_) =>
            logger.info("unbinding succeeded; shutting down elasticsearch, mongo and kafka clients")
            EsBulkClient.closeConnection()
            EsSimpleClient.closeConnection()
            eolConsumer.drainAndShutdown()
            mongo.close()
            system.terminate()

          case Failure(f) =>
            logger.error("shutdown failed", f)
            EsBulkClient.closeConnection()
            EsSimpleClient.closeConnection()
            mongo.close()
            system.terminate()

        }

      }

    })

  }

}
