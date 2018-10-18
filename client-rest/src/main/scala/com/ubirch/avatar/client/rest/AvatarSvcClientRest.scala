package com.ubirch.avatar.client.rest

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.client.rest.config.AvatarClientRestConfig
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceClaim, DeviceDataRaw, DeviceInfo, DeviceStateUpdate, DeviceUserClaim}
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.{JsonErrorResponse, JsonResponse}

import org.json4s.native.Serialization.read

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.stream.Materializer
import akka.util.ByteString

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * author: cvandrei
  * since: 2018-10-17
  */
object AvatarSvcClientRest extends MyJsonProtocol
  with StrictLogging {

  def check()(implicit httpClient: HttpExt, materializer: Materializer): Future[Option[JsonResponse]] = {

    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    val url = AvatarClientRestConfig.urlCheck
    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[JsonResponse](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        Future(
          logErrorAndReturnNone(s"check() call to avatar-service failed: url=$url code=$code, status=${res.status}")
        )

    }

  }

  def deepCheck()(implicit httpClient: HttpExt, materializer: Materializer): Future[DeepCheckResponse] = {

    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    val statusCodes: Set[StatusCode] = Set(StatusCodes.OK, StatusCodes.ServiceUnavailable)

    val url = AvatarClientRestConfig.urlDeepCheck
    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(status, _, entity, _) if statusCodes.contains(status) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          read[DeepCheckResponse](body.utf8String)
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        val errorText = s"deepCheck() call to avatar-service failed: url=$url code=$code, status=${res.status}"
        logger.error(errorText)
        val deepCheckRes = DeepCheckResponse(status = false, messages = Seq(errorText))
        Future(
          DeepCheckResponseUtil.addServicePrefix("avatar-service", deepCheckRes)
        )

    }

  }

  /**
    * Update a device by POSTing raw device data.
    *
    * @param deviceDataRaw raw data to POST
    * @return http response
    */
  def deviceUpdatePOST(deviceDataRaw: DeviceDataRaw)
                      (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, DeviceStateUpdate]] = {

    // TODO UP-297: automated tests
    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    Json4sUtil.any2String(deviceDataRaw) match {

      case Some(deviceDataRawString) =>

        val url = AvatarClientRestConfig.urlDeviceUpdate
        val req = HttpRequest(
          method = HttpMethods.POST,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(deviceDataRawString))
        )
        httpClient.singleRequest(req) flatMap {

          case HttpResponse(StatusCodes.OK, _, entity, _) =>

            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Right(read[DeviceStateUpdate](body.utf8String))
            }

          case res@HttpResponse(code, _, entity, _) =>

            logger.error(s"deviceUpdatePOST() call to avatar-service failed: url=$url code=$code, status=${res.status}")
            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Left(read[JsonErrorResponse](body.utf8String))
            }

        }

      case None =>

        logger.error(s"failed to to convert input to JSON: deviceDataRaw=$deviceDataRaw")
        Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: failed to convert input to JSON")))

    }

  }

  /**
    * @param deviceDataRaw device data to post
    * @param oidcToken     OpenID Connect token to use for authorization
    * @param ubirchToken   ubirch token to use for authorization (ignored if oidcToken is set)
    * @param httpClient    http connection
    * @param materializer  Akka materializer required by http connection
    */
  def deviceUpdateBulkPOST(deviceDataRaw: DeviceDataRaw,
                           oidcToken: Option[String],
                           ubirchToken: Option[String] = None
                          )
                          (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, JsonResponse]] = {

    // TODO UP-297: automated tests
    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    Json4sUtil.any2String(deviceDataRaw) match {

      case Some(deviceDataRawString) =>

        val url = AvatarClientRestConfig.urlDeviceUpdateBulk
        // TODO set auth header
        val req = HttpRequest(
          method = HttpMethods.POST,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(deviceDataRawString))
        )
        httpClient.singleRequest(req) flatMap {

          case HttpResponse(StatusCodes.OK, _, entity, _) =>

            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Right(read[JsonResponse](body.utf8String))
            }

          case res@HttpResponse(code, _, entity, _) =>

            logger.error(s"deviceUpdateBulkPOST() call to avatar-service failed: url=$url code=$code, status=${res.status}")
            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Left(read[JsonErrorResponse](body.utf8String))
            }

        }

      case None =>

        logger.error(s"failed to to convert input to JSON: deviceDataRaw=$deviceDataRaw")
        Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: failed to convert input to JSON")))

    }

  }

  /**
    * @param device       device to create
    * @param oidcToken    OpenID Connect token to use for authorization
    * @param ubirchToken  ubirch token to use for authorization (ignored if oidcToken is set)
    * @param httpClient   http connection
    * @param materializer Akka materializer required by http connection
    */
  def devicePOST(device: Device,
                 oidcToken: Option[String] = None,
                 ubirchToken: Option[String] = None
                )
                (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, Device]] = {

    // TODO UP-297: automated tests
    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    if (oidcToken.isEmpty && ubirchToken.isEmpty) {

      logger.error(s"either an OpenID Connect or ubirch token is needed to create a device: device=$device")
      Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token")))

    } else {

      Json4sUtil.any2String(device) match {

        case Some(deviceString) =>

          val url = AvatarClientRestConfig.urlDevice
          // TODO set auth header
          val req = HttpRequest(
            method = HttpMethods.POST,
            uri = url,
            entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(deviceString))
          )
          httpClient.singleRequest(req) flatMap {

            case HttpResponse(StatusCodes.OK, _, entity, _) =>

              entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
                Right(read[Device](body.utf8String))
              }

            case res@HttpResponse(code, _, entity, _) =>

              logger.error(s"devicePOST() call to avatar-service failed: url=$url code=$code, status=${res.status}")
              entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
                Left(read[JsonErrorResponse](body.utf8String))
              }

          }

        case None =>

          logger.error(s"failed to to convert input to JSON: device=$device")
          Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: failed to convert input to JSON")))

      }

    }

  }

  /**
    * @param oidcToken    OpenID Connect token to use for authorization
    * @param ubirchToken  ubirch token to use for authorization (ignored if oidcToken is set)
    * @param httpClient   http connection
    * @param materializer Akka materializer required by http connection
    */
  def deviceStubGET(oidcToken: Option[String] = None,
                    ubirchToken: Option[String] = None
                   )
                   (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, Set[DeviceInfo]]] = {

    // TODO UP-297: automated tests
    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    if (oidcToken.isEmpty && ubirchToken.isEmpty) {

      logger.error("either an OpenID Connect or ubirch token is needed to query device stubs")
      Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")))

    } else {

      val url = AvatarClientRestConfig.urlDeviceStub
      // TODO set auth header
      val req = HttpRequest(
        method = HttpMethods.GET,
        uri = url
      )
      httpClient.singleRequest(req) flatMap {

        case HttpResponse(StatusCodes.OK, _, entity, _) =>

          entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
            Right(read[Set[DeviceInfo]](body.utf8String))
          }

        case res@HttpResponse(code, _, entity, _) =>

          logger.error(s"deviceStubGET() call to avatar-service failed: url=$url code=$code, status=${res.status}")
          entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
            Left(read[JsonErrorResponse](body.utf8String))
          }

      }

    }

  }

  /**
    * @param oidcToken    OpenID Connect token to use for authorization
    * @param ubirchToken  ubirch token to use for authorization (ignored if oidcToken is set)
    * @param httpClient   http connection
    * @param materializer Akka materializer required by http connection
    */
  def deviceGET(oidcToken: Option[String] = None,
                ubirchToken: Option[String] = None
               )
               (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, Set[Device]]] = {

    // TODO UP-297: automated tests
    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    if (oidcToken.isEmpty && ubirchToken.isEmpty) {

      logger.error("either an OpenID Connect or ubirch token is needed to query devices")
      Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")))

    } else {

      val url = AvatarClientRestConfig.urlDevice
      // TODO set auth header
      val req = HttpRequest(
        method = HttpMethods.GET,
        uri = url
      )
      httpClient.singleRequest(req) flatMap {

        case HttpResponse(StatusCodes.OK, _, entity, _) =>

          entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
            Right(read[Set[Device]](body.utf8String))
          }

        case res@HttpResponse(code, _, entity, _) =>

          logger.error(s"deviceGET() call to avatar-service failed: url=$url code=$code, status=${res.status}")
          entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
            Left(read[JsonErrorResponse](body.utf8String))
          }

      }

    }

  }

  /**
    * @param device       updated device
    * @param oidcToken    OpenID Connect token to use for authorization
    * @param ubirchToken  ubirch token to use for authorization (ignored if oidcToken is set)
    * @param httpClient   http connection
    * @param materializer Akka materializer required by http connection
    */
  def deviceIdPUT(device: Device,
                  oidcToken: Option[String] = None,
                  ubirchToken: Option[String] = None
                 )
                 (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, Device]] = {

    // TODO UP-297: automated tests
    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    if (oidcToken.isEmpty && ubirchToken.isEmpty) {

      logger.error("either an OpenID Connect or ubirch token is needed to update a device")
      Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")))

    } else {

      Json4sUtil.any2String(device) match {

        case Some(deviceString) =>

          val url = AvatarClientRestConfig.urlDeviceWithId(device.deviceId)
          // TODO set auth header
          val req = HttpRequest(
            method = HttpMethods.PUT,
            uri = url,
            entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(deviceString))
          )
          httpClient.singleRequest(req) flatMap {

            case HttpResponse(StatusCodes.OK, _, entity, _) =>

              entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
                Right(read[Device](body.utf8String))
              }

            case res@HttpResponse(code, _, entity, _) =>

              logger.error(s"deviceIdPUT() call to avatar-service failed: url=$url code=$code, status=${res.status}")
              entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
                Left(read[JsonErrorResponse](body.utf8String))
              }

          }

        case None =>

          logger.error(s"failed to to convert input to JSON: device=$device")
          Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: failed to convert input to JSON")))

      }

    }

  }

  /**
    * @param deviceId     device to delete
    * @param oidcToken    OpenID Connect token to use for authorization
    * @param ubirchToken  ubirch token to use for authorization (ignored if oidcToken is set)
    * @param httpClient   http connection
    * @param materializer Akka materializer required by http connection
    */
  def deviceIdDELETE(deviceId: UUID,
                     oidcToken: Option[String] = None,
                     ubirchToken: Option[String] = None
                    )
                    (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, Boolean]] = {

    // TODO UP-297: automated tests
    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    if (oidcToken.isEmpty && ubirchToken.isEmpty) {

      logger.error("either an OpenID Connect or ubirch token is needed to delete a device")
      Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")))

    } else {

      val url = AvatarClientRestConfig.urlDeviceWithId(deviceId.toString)
      // TODO set auth header
      val req = HttpRequest(
        method = HttpMethods.DELETE,
        uri = url
      )
      httpClient.singleRequest(req) flatMap {

        case res@HttpResponse(StatusCodes.OK, _, entity, _) =>

          res.discardEntityBytes()
          Future(Right(true))

        case res@HttpResponse(code, _, entity, _) =>

          logger.error(s"deviceIdDELETE() call to avatar-service failed: url=$url code=$code, status=${res.status}")
          entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
            Left(read[JsonErrorResponse](body.utf8String))
          }

      }

    }

  }

  /**
    * @param hwDeviceId device to claim
    * @param oidcToken    OpenID Connect token to use for authorization
    * @param ubirchToken  ubirch token to use for authorization (ignored if oidcToken is set)
    * @param httpClient   http connection
    * @param materializer Akka materializer required by http connection
    */
  def claimDevicePUT(hwDeviceId: String,
                     oidcToken: Option[String] = None,
                     ubirchToken: Option[String] = None
                    )
                    (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, DeviceUserClaim]] = {

    // TODO UP-297: automated tests
    implicit val ec: ExecutionContextExecutor = materializer.executionContext
    if (oidcToken.isEmpty && ubirchToken.isEmpty) {

      logger.error("either an OpenID Connect or ubirch token is needed to claim a device")
      Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: either an OpenID Connect or ubirch token is missing")))

    } else {

      val deviceClaim = DeviceClaim(hwDeviceId = hwDeviceId)
      Json4sUtil.any2String(deviceClaim) match {

        case Some(deviceClaimString) =>

          val url = AvatarClientRestConfig.urlDeviceClaim
          // TODO set auth header
          val req = HttpRequest(
            method = HttpMethods.PUT,
            uri = url,
            entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(deviceClaimString))
          )
          httpClient.singleRequest(req) flatMap {

            case HttpResponse(StatusCodes.OK, _, entity, _) =>

              entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
                Right(read[DeviceUserClaim](body.utf8String))
              }

            case res@HttpResponse(code, _, entity, _) =>

              logger.error(s"claimDevicePUT() call to avatar-service failed: url=$url code=$code, status=${res.status}")
              entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
                Left(read[JsonErrorResponse](body.utf8String))
              }

          }

        case None =>

          logger.error(s"failed to to convert input to JSON: hwDeviceId=$hwDeviceId")
          Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: failed to convert input to JSON")))

      }

    }

  }

  private def logErrorAndReturnNone[T](errorMsg: String,
                                       t: Option[Throwable] = None
                                      ): Option[T] = {
    t match {
      case None => logger.error(errorMsg)
      case Some(someThrowable: Throwable) => logger.error(errorMsg, someThrowable)
    }

    None

  }

}
