package com.ubirch.avatar.client.rest

import com.ubirch.avatar.config.Config
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.model.JsonResponse

import org.scalatest.{BeforeAndAfterAll, FeatureSpec, Matchers}

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

/**
  * author: cvandrei
  * since: 2018-10-18
  */
class AvatarSvcClientRestSpec extends FeatureSpec
  with Matchers
  with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val httpClient: HttpExt = Http()

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
    httpClient.shutdownAllConnectionPools()
    Thread.sleep(500)
    System.exit(0)
  }

  feature("check()") {

    scenario("check without errors") {

      // test
      AvatarSvcClientRest.check() map {

        // verify
        case None => fail("expected a result other than None")

        case Some(jsonResponse: JsonResponse) =>
          val goInfo = s"${Config.goPipelineName} / ${Config.goPipelineLabel} / ${Config.goPipelineRevision}"
          val expected = JsonResponse(message = s"Welcome to the ubirchTemplateService ( $goInfo )")
          jsonResponse shouldBe expected

      }

    }

  }

  feature("deepCheck()") {

    scenario("check without errors") {

      // test
      AvatarSvcClientRest.deepCheck() map { deepCheckResponse =>

        // verify
        deepCheckResponse shouldBe DeepCheckResponse()

      }

    }

  }

}
