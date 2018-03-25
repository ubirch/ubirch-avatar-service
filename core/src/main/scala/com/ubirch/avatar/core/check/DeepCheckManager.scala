package com.ubirch.avatar.core.check

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.avatar.AvatarStateManager
import com.ubirch.keyservice.client.rest.KeyServiceClientRest
import com.ubirch.user.client.rest.UserServiceClientRest
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
object DeepCheckManager {

  /**
    * Check if we can run a simple query on the database.
    *
    * @return deep check response with _status:OK_ if ok; otherwise with _status:NOK_
    */
  def connectivityCheck()(implicit mongo: MongoUtil, _system: ActorSystem, httpClient: HttpExt, materializer: Materializer): Future[DeepCheckResponse] = {

    //@REVIEW
    // TODO check MQTT connection
    // TODO check SQS connections
    // TODO check Redis connections
    // TODO check Auth connections

    for {

      // direct dependencies

      esDeepCheck <- ESSimpleStorage.connectivityCheck(
        docIndex = Config.esDeviceDataRawIndex,
        docType = Config.esDeviceDataRawType
      )

      esDeepCheckWithPrefix = DeepCheckResponseUtil.addServicePrefix("avatar-service", esDeepCheck)
      mongoConnectivity <- AvatarStateManager.connectivityCheck()
      redisConnectivity <- RedisClientUtil.connectivityCheck("avatar-service")

      // other services
      keyDeepCheck <- KeyServiceClientRest.deepCheck()
      userDeepCheck <- UserServiceClientRest.deepCheck()

    } yield {

      DeepCheckResponseUtil.merge(
        Seq(
          esDeepCheckWithPrefix,
          mongoConnectivity,
          redisConnectivity,
          keyDeepCheck,
          userDeepCheck
        )
      )

    }

  }

}
