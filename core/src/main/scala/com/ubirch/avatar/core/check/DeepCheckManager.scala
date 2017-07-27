package com.ubirch.avatar.core.check

import com.ubirch.keyservice.client.rest.KeyServiceClientRest
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
  def connectivityCheck()(implicit httpClient: HttpExt, materializer: Materializer): Future[DeepCheckResponse] = {

    for {

      esDeepCheck <- ESSimpleStorage.connectivityCheck()
      keyDeepCheckOpt <- KeyServiceClientRest.deepCheck()
      //userDeepCheck <- UserServiceClientRest.deepCheck()

    } yield {

      val keyDeepCheck = keyDeepCheckOpt match {
        case None => DeepCheckResponse(status = false, messages = Seq("key-service: deepCheck() failed"))
        case Some(deepCheck) => deepCheck
      }
      val responses: Set[DeepCheckResponse] = Set(esDeepCheck, keyDeepCheck)
      val resultingStatus: Boolean = responses.forall(n => n.status)
      val resultingMessages: Seq[String] = responses.foldLeft(Nil: Seq[String]) { (m: Seq[String], n: DeepCheckResponse) =>
        m ++ n.messages
      }

      DeepCheckResponse(
        status = resultingStatus,
        messages = resultingMessages
      )

      // TODO check user-service connection

    }



    // TODO check MQTT connection
    // TODO check SQS connections
    // TODO check Mongo connection
    // TODO check Redis connection

  }

}
