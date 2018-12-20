package com.ubirch.avatar.core.check

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.avatar.AvatarStateManager
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.redis.RedisClientUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-08
  */
object ReadyCheckManager extends StrictLogging {

  /**
    * Check if we can run a simple query on the database.
    *
    * @return deep check response with _status:OK_ if ok; otherwise with _status:NOK_
    */
  def connectivityCheck()(implicit _system: ActorSystem, httpClient: HttpExt, materializer: Materializer, mongo: MongoUtil): Future[DeepCheckResponse] = {

    (for {

      // direct dependencies

      esConnectivity <- ESSimpleStorage.connectivityCheck(
        docIndex = Config.esDeviceDataRawIndex,
        docType = Config.esDeviceDataRawType
      ).map { res =>
        DeepCheckResponseUtil.addServicePrefix("[avatar-service.elasticsearch]", res)
      }

      mongoConnectivity <- AvatarStateManager.connectivityCheck("avatar-service.mongo")

      redisConnectivity <- RedisClientUtil.connectivityCheck("avatar-service.redis")

    } yield {

      DeepCheckResponseUtil.merge(
        Seq(
          esConnectivity,
          mongoConnectivity,
          redisConnectivity
        )
      )

    }).recover {
      case e =>
        logger.error("connectivityCheck", e)
        DeepCheckResponse(false, Seq(e.getMessage))
    }
  }

}
