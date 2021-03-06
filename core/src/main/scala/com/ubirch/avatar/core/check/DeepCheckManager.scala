package com.ubirch.avatar.core.check

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.idservice.client.IdServiceClientCached
import com.ubirch.user.client.UserServiceClient
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-08
  */
object DeepCheckManager extends StrictLogging {

  /**
    * Check if we can run a simple query on the database.
    *
    * @return deep check response with _status:OK_ if ok; otherwise with _status:NOK_
    */
  def connectivityCheck()(implicit _system: ActorSystem, httpClient: HttpExt, materializer: Materializer, mongo: MongoUtil): Future[DeepCheckResponse] = {

    //@REVIEW
    // TODO check MQTT connection
    // TODO check SQS connections
    // TODO check Auth connections

    (for {

      readyCheck <- ReadyCheckManager.connectivityCheck()

      // other services
      keyDeepCheck <- IdServiceClientCached.deepCheck().map { res =>
        DeepCheckResponseUtil.addServicePrefix("[avatar-service.key-service]", res)
      }.recover {
        case e =>
          logger.error("KeyService connectivityCheck", e)
          DeepCheckResponse(status = false, Seq(s"key-service error: ${e.getMessage}"))
      }

      userDeepCheck <- UserServiceClient.deepCheck().map { res =>
        DeepCheckResponseUtil.addServicePrefix("[avatar-service.user-service]", res)
      }.recover {
        case e =>
          logger.error("UserService connectivityCheck", e)
          DeepCheckResponse(status = false, Seq(s"user-service error: ${e.getMessage}"))
      }

    } yield {

      DeepCheckResponseUtil.merge(
        Seq(
          readyCheck,
          keyDeepCheck,
          userDeepCheck
        )
      )

    }).recover {
      case e =>
        logger.error("connectivityCheck", e)
        DeepCheckResponse(status = false, Seq(s"deep check error: ${e.getMessage}"))
    }
  }

}
