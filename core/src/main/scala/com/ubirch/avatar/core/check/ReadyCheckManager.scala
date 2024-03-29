package com.ubirch.avatar.core.check

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.avatar.AvatarStateManager
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.elasticsearch.EsSimpleClient
import com.ubirch.util.mongo.connection.MongoUtil

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
  def connectivityCheck()(
    implicit _system: ActorSystem,
    httpClient: HttpExt,
    materializer: Materializer,
    mongo: MongoUtil): Future[DeepCheckResponse] = {

    (for {

      // direct dependencies
      esConnectivity <- EsSimpleClient.connectivityCheck(
        docIndex = Config.esDeviceIndex
      ).map { res =>
        DeepCheckResponseUtil.addServicePrefix("[avatar-service.elasticsearch]", res)
      }

      mongoConnectivity <- AvatarStateManager.connectivityCheck("avatar-service.mongo")

    } yield {

      DeepCheckResponseUtil.merge(
        Seq(
          esConnectivity,
          mongoConnectivity
        )
      )

    }).recover {
      case e =>
        logger.error("connectivityCheck", e)
        DeepCheckResponse(status = false, Seq(e.getMessage))
    }
  }

}
