package com.ubirch.avatar.client.rest

import java.net.URL

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.client.rest.config.AvatarClientConfig
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceClaim, DeviceDataRaw, DeviceInfo, DeviceUserClaim}
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonErrorResponse

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.header.{Header, Headers}
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.{Response, Status}

/**
  * author: cvandrei
  * since: 2016-11-15
  */
object AvatarRestClient
  extends StrictLogging
    with MyJsonProtocol {

  private val httpClient = new HttpClient(commonConfig = uk.co.bigbeeconsultants.http.Config(
    connectTimeout = AvatarClientConfig.timeoutConnect,
    readTimeout = AvatarClientConfig.timeoutRead
  ))

  private def baseUrl = AvatarClientConfig.avatarBaseUrl

  /**
    * Update a device by POSTing raw device data.
    *
    * @param deviceDataRaw raw data to POST
    * @return http response
    */
  def deviceUpdatePOST(deviceDataRaw: DeviceDataRaw): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceUpdate}")
    logger.debug(s"try to call REST endpoint: $url")

    httpClient.post(
      url = url,
      body = Some(RequestBody(Json4sUtil.any2String(deviceDataRaw).get, APPLICATION_JSON))
    )

  }

  /**
    * Bulk update a device by POSTing raw device data.
    *
    * @param deviceDataRaw raw data to POST
    * @param oidcToken   OIDC token of the user whose device stubs to list
    * @param ubirchToken ubirch token of the user whose device stubs to list
    * @return http response
    */
  def deviceBulkPOST(deviceDataRaw: DeviceDataRaw,
                     oidcToken: Option[String],
                     ubirchToken: Option[String] = None
                    ): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceBulk}")
    logger.debug(s"try to call REST endpoint: $url")

    httpClient.post(
      url,
      body = Some(RequestBody(Json4sUtil.any2String(deviceDataRaw).get, APPLICATION_JSON)),
      requestHeaders = authHeaders(oidcToken = oidcToken, ubirchToken = ubirchToken)
    )

  }

  def devicePOST(device: Device,
                 oidcToken: Option[String],
                 ubirchToken: Option[String] = None
                ): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDevice}")
    logger.debug(s"try to call REST endpoint: $url")

    httpClient.post(
      url = url,
      body = Some(RequestBody(Json4sUtil.any2String(device).get, APPLICATION_JSON)),
      requestHeaders = authHeaders(oidcToken = oidcToken, ubirchToken = ubirchToken)
    )

  }

  /**
    * @param oidcToken   OIDC token of the user whose device stubs to list
    * @param ubirchToken ubirch token of the user whose device stubs to list
    * @return None in case of an error; other a sequence (empty if no devices are found)
    */
  def deviceStubGET(oidcToken: Option[String], ubirchToken: Option[String] = None): Option[Set[DeviceInfo]] = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceStub}")
    logger.debug(s"try to call REST endpoint: $url")

    val res = httpClient.get(
      url = url,
      requestHeaders = authHeaders(oidcToken = oidcToken, ubirchToken = ubirchToken)
    )

    if (res.status == Status.S200_OK) {

      val jsonString = res.body.asString
      val deviceInfos = Json4sUtil.string2any[Set[DeviceInfo]](jsonString)
      Some(deviceInfos)

    } else {

      logger.error(s"failed to query device stubs: response=$res")
      None

    }

  }

  /**
    * @param oidcToken   OIDC token of the user whose device to list
    * @param ubirchToken ubirch token of the user whose device to list
    * @return None in case of an error; other a sequence (empty if no devices are found)
    */
  def deviceGET(oidcToken: Option[String], ubirchToken: Option[String] = None): Option[Set[Device]] = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDevice}")
    logger.debug(s"try to call REST endpoint: $url")

    val res = httpClient.get(
      url = url,
      requestHeaders = authHeaders(oidcToken = oidcToken, ubirchToken = ubirchToken)
    )

    if (res.status == Status.S200_OK) {

      val jsonString = res.body.asString
      val deviceInfos = Json4sUtil.string2any[Set[Device]](jsonString)
      Some(deviceInfos)

    } else {

      logger.error(s"failed to query devices: response=$res")
      None

    }

  }

  /**
    * @param device      updated device
    * @param oidcToken   OIDC token of the user whose device to update
    * @param ubirchToken ubirch token of the user whose device to update
    * @return None in case of an error; otherwise the updated device
    */
  def deviceIdPUT(device: Device,
                  oidcToken: Option[String],
                  ubirchToken: Option[String] = None
                 ): Option[Device] = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceWithId(device.deviceId)}")
    logger.debug(s"try to call REST endpoint: $url")

    val res = httpClient.put(
      url = url,
      body = RequestBody(Json4sUtil.any2String(device).get, APPLICATION_JSON),
      requestHeaders = authHeaders(oidcToken = oidcToken, ubirchToken = ubirchToken)
    )

    if (res.status == Status.S200_OK) {

      val jsonString = res.body.asString
      val deviceInfos = Json4sUtil.string2any[Device](jsonString)
      Some(deviceInfos)

    } else {

      logger.error(s"failed to update device: response=$res")
      None

    }

  }

  /**
    * this method could be used to claim a device by current user (identified by Auth Token)
    * the claimed device may not be owned by an other user
    *
    * @param hwDeviceId  hardware device id as a String
    * @param oidcToken   OIDC token of the user claiming a device
    * @param ubirchToken ubirch token of the user claiming a device
    * @return Boolean value
    */

  def claimDevice(hwDeviceId: String,
                  oidcToken: Option[String],
                  ubirchToken: Option[String] = None
                 ): DeviceUserClaim = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceClaim}")
    logger.debug(s"try to call REST endpoint: $url")
    val headers: Headers = authHeaders(oidcToken = oidcToken, ubirchToken = ubirchToken)

    val deviceClaim = DeviceClaim(hwDeviceId = hwDeviceId)
    val deviceClaimJsonStr = Json4sUtil.any2String(deviceClaim).get

    val body = RequestBody(deviceClaimJsonStr, APPLICATION_JSON)

    httpClient.put(url, body, headers) match {
      case resp if resp.status == Status.S202_Accepted =>

        val respStr = resp.body.asString

        Json4sUtil.string2JValue(respStr) match {
          case Some(jval) =>

            jval.extractOpt[DeviceUserClaim] match {
              case Some(duc) =>
                duc
              case None =>
                throw new Exception(s"got invalid response: $respStr")
            }

          case None =>
            throw new Exception(s"got invalid response: $respStr")
        }

      case resp =>
        Json4sUtil.string2any[JsonErrorResponse](resp.body.asString) match {
          case jer =>
            throw new Exception(jer.errorMessage)
        }
    }

  }

  /**
    * Generates the authorization header depending on which one we have. The OIDC token always has priority.
    *
    * @param oidcToken   OIDS token to authorize with
    * @param ubirchToken ubirch token to authorize with (ignored if OIDC token is defined)
    * @return list of either OIDC token or ubirch token; exception if none is defined
    */
  private def authHeaders(oidcToken: Option[String], ubirchToken: Option[String] = None): Headers = {

    require(oidcToken.isDefined || ubirchToken.isDefined, "OIDC token or ubirch token may be defined")

    if (oidcToken.isDefined) {
      Headers(List(Header(name = "Authorization", value = s"Bearer ${oidcToken.get}")))
    } else if (ubirchToken.isDefined) {
      Headers(List(Header(name = "Authorization", value = ubirchToken.get)))
    } else {
      List.empty
    }

  }

}
