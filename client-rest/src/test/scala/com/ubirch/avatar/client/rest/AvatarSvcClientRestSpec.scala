//package com.ubirch.avatar.client.rest
//
//import com.ubirch.avatar.config.Config
//import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
//import com.ubirch.util.deepCheck.model.DeepCheckResponse
//import com.ubirch.util.model.{JsonErrorResponse, JsonResponse}
//import com.ubirch.util.uuid.UUIDUtil
//
//import org.scalatest.{AsyncFeatureSpec, BeforeAndAfterAll, BeforeAndAfterEach, Matchers}
//
//import akka.actor.ActorSystem
//import akka.http.scaladsl.{Http, HttpExt}
//import akka.stream.ActorMaterializer
//
//import scala.concurrent.ExecutionContextExecutor
//
///**
//  * author: cvandrei
//  * since: 2018-10-18
//  */
//class AvatarSvcClientRestSpec extends AsyncFeatureSpec
//  with Matchers
//  with BeforeAndAfterEach
//  with BeforeAndAfterAll {
//
//  implicit val system: ActorSystem = ActorSystem()
//  implicit val ec: ExecutionContextExecutor = system.dispatcher
//  implicit val materializer: ActorMaterializer = ActorMaterializer()
//  implicit val httpClient: HttpExt = Http()
//
//  override protected def afterAll(): Unit = {
//    super.afterAll()
//    system.terminate()
//    httpClient.shutdownAllConnectionPools()
//    Thread.sleep(500)
//    System.exit(0)
//  }
//
//  // TODO add database clean up before each test (elasticsearch, redis, neo4j and mongo)
//
//  feature("check()") {
//
//    scenario("check without errors") {
//
//      // test
//      AvatarSvcClientRest.check() map {
//
//        // verify
//        case None => fail("expected a result other than None")
//
//        case Some(jsonResponse: JsonResponse) =>
//          val goInfo = s"${Config.goPipelineName} / ${Config.goPipelineLabel} / ${Config.goPipelineRevision}"
//          val expected = JsonResponse(message = s"Welcome to the ubirchAvatarService ( $goInfo )")
//          jsonResponse shouldBe expected
//
//      }
//
//    }
//
//  }
//
//  feature("deepCheck()") {
//
//    scenario("check without errors") {
//
//      // test
//      AvatarSvcClientRest.deepCheck() map { deepCheckResponse =>
//
//        // verify
//        deepCheckResponse shouldBe DeepCheckResponse()
//
//      }
//
//    }
//
//  }
//
//  /*
//  feature("deviceUpdatePOST()") {
//
//    // TODO add tests
//
//  }
//  */
//
//  feature("deviceUpdateBulkPOST()") {
//
//    scenario("without any token --> error") {
//
//      // prepare
//      val device = DummyDevices.device()
//      val deviceData = DummyDeviceDataRaw.data(device = device)
//
//      // test
//      AvatarSvcClientRest.deviceUpdateBulkPOST(deviceDataRaw = deviceData) map {
//
//        case Right(jsonResponse: JsonResponse) =>
//
//          jsonResponse shouldBe JsonResponse(message = "processing started")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//  }
//
//  feature("devicePOST()") {
//
//    scenario("without any token --> error") {
//
//      // prepare
//      val device = DummyDevices.device()
//
//      // test
//      AvatarSvcClientRest.deviceIdPUT(device = device) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (oidc) --> error") {
//
//      // prepare
//      val device = DummyDevices.device()
//
//      // test
//      AvatarSvcClientRest.deviceIdPUT(device = device, oidcToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (ubirch token) --> error") {
//
//      // prepare
//      val device = DummyDevices.device()
//
//      // test
//      AvatarSvcClientRest.deviceIdPUT(device = device, ubirchToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    // TODO scenario("valid auth (oidc)        ; device does not exist --> success") {}
//    // TODO scenario("valid auth (ubirch token); device does not exist --> success") {}
//
//    // TODO scenario("valid auth (oidc)        ; device exists --> success") {}
//    // TODO scenario("valid auth (ubirch token); device exists --> success") {}
//
//  }
//
//  feature("deviceStubGET()") {
//
//    scenario("without any token --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceStubGET() map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (oidc) --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceStubGET(oidcToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (ubirch token) --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceStubGET(ubirchToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    // TODO scenario("valid auth (oidc)        ; has no devices --> empty device stub list") {}
//    // TODO scenario("valid auth (ubirch token); has no devices --> empty device stub list") {}
//
//    // TODO scenario("valid auth (oidc)        ; has devices --> non-empty device stub list") {}
//    // TODO scenario("valid auth (ubirch token); has devices --> non-empty device stub list") {}
//
//  }
//
//  feature("deviceGET()") {
//
//    scenario("without any token --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceGET() map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (oidc) --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceGET(oidcToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (ubirch token) --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceGET(ubirchToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    // TODO scenario("valid auth (oidc);         has no devices --> empty device list") {}
//    // TODO scenario("valid auth (ubirch token); has no devices --> empty device list") {}
//
//    // TODO scenario("valid auth (oidc);         has devices --> non-empty device list") {}
//    // TODO scenario("valid auth (ubirch token); has devices --> non-empty device list") {}
//
//  }
//
//  feature("deviceIdPUT()") {
//
//    scenario("without any token --> error") {
//
//      // prepare
//      val device = DummyDevices.device()
//
//      // test
//      AvatarSvcClientRest.deviceIdPUT(device = device) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (oidc) --> error") {
//
//      // prepare
//      val device = DummyDevices.device()
//
//      // test
//      AvatarSvcClientRest.deviceIdPUT(device = device, oidcToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (ubirch token) --> error") {
//
//      // prepare
//      val device = DummyDevices.device()
//
//      // test
//      AvatarSvcClientRest.deviceIdPUT(device = device, ubirchToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    // TODO scenario("valid auth(oidc)        ; device does not exist --> error") {}
//    // TODO scenario("valid auth(ubirch token); device does not exist --> error") {}
//
//    // TODO scenario("valid auth (oidc); device exists and belongs to same user --> success") {}
//    // TODO scenario("valid auth (ubirch token); device exists and belongs to same user --> success") {}
//
//    // TODO scenario("valid auth (oidc)        ; device exists but belongs to another user --> error") {}
//    // TODO scenario("valid auth (ubirch token); device exists but belongs to another user --> error") {}
//
//  }
//
//  feature("deviceIdDELETE()") {
//
//    scenario("without any token --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceIdDELETE(deviceId = UUIDUtil.uuid) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (oidc) --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceIdDELETE(deviceId = UUIDUtil.uuid, oidcToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (ubirch token) --> error") {
//
//      // test
//      AvatarSvcClientRest.deviceIdDELETE(deviceId = UUIDUtil.uuid, ubirchToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    // TODO scenario("valid auth (oidc)        ; device does not exist --> success") {}
//    // TODO scenario("valid auth (ubirch token); device does not exist --> success") {}
//
//    // TODO scenario("valid auth (oidc)        ; device belongs to another user --> error") {}
//    // TODO scenario("valid auth (ubirch token); device belongs to another user --> error") {}
//
//    // TODO scenario("valid auth (oidc)        ; device belongs to same user --> success") {}
//    // TODO scenario("valid auth (ubirch token); device belongs to same user --> success") {}
//
//  }
//
//  feature("claimDevicePUT()") {
//
//    scenario("without any token --> error") {
//
//      // test
//      AvatarSvcClientRest.claimDevicePUT(hwDeviceId = UUIDUtil.uuidStr) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (oidc) --> error") {
//
//      // test
//      AvatarSvcClientRest.claimDevicePUT(hwDeviceId = UUIDUtil.uuidStr, oidcToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    scenario("invalid auth (ubirch token) --> error") {
//
//      // test
//      AvatarSvcClientRest.claimDevicePUT(hwDeviceId = UUIDUtil.uuidStr, ubirchToken = Some("invalid-token")) map {
//
//        case Left(error: JsonErrorResponse) =>
//
//          error shouldBe JsonErrorResponse(errorType = "InvalidToken", errorMessage = "login token is not valid")
//
//        case _ =>
//
//          fail("request should have produced an error")
//
//      }
//
//    }
//
//    // TODO scenario("valid auth(oidc)        ; device does not exist --> error") {}
//    // TODO scenario("valid auth(ubirch token); device does not exist --> error") {}
//
//    // TODO scenario("valid auth (oidc)        ; device has been claimed already --> ???") {}
//    // TODO scenario("valid auth (ubirch token); device has been claimed already --> ???") {}
//
//    // TODO scenario("valid auth (oidc)        ; device has not been claimed --> success") {}
//    // TODO scenario("valid auth (ubirch token); device has not been claimed --> success") {}
//
//  }
//
//}
