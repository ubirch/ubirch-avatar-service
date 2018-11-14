package com.ubirch.avatar.client.rest

import java.net.URL

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.client.rest.config.AvatarClientConfig
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.header.{Header, Headers}
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.Response

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
  @deprecated("migrate to `AvatarSvcClientRest.deviceUpdatePOST()` instead since this method will be deleted rather sooner than later", "0.6.0-SNAPSHOT")
  def deviceUpdatePOST(deviceDataRaw: DeviceDataRaw): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceUpdate}")
    logger.debug(s"try to call REST endpoint: POST $url")

    httpClient.post(
      url = url,
      body = Some(RequestBody(Json4sUtil.any2String(deviceDataRaw).get, APPLICATION_JSON))
    )

  }

  /**
    * Bulk update a device by POSTing raw device data.
    *
    * @param deviceDataRaw raw data to POST
    * @param oidcToken     OIDC token of the user whose device stubs to list
    * @param ubirchToken   ubirch token of the user whose device stubs to list
    * @return http response
    */
  @deprecated("migrate to `AvatarSvcClientRest.deviceUpdateBulkPOST()` instead since this method will be deleted rather sooner than later", "0.6.0-SNAPSHOT")
  def deviceUpdateBulkPOST(deviceDataRaw: DeviceDataRaw,
                           oidcToken: Option[String],
                           ubirchToken: Option[String] = None
                          ): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceBulk}")
    logger.debug(s"try to call REST endpoint: POST $url")

    httpClient.post(
      url,
      body = Some(RequestBody(Json4sUtil.any2String(deviceDataRaw).get, APPLICATION_JSON)),
      requestHeaders = authHeaders(oidcToken = oidcToken, ubirchToken = ubirchToken)
    )

  }

  @deprecated("migrate to `AvatarSvcClientRest.devicePOST()` instead since this method will be deleted rather sooner than later", "0.6.0-SNAPSHOT")
  def devicePOST(device: Device,
                 oidcToken: Option[String],
                 ubirchToken: Option[String] = None
                ): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDevice}")
    logger.debug(s"try to call REST endpoint: POST $url")

    httpClient.post(
      url = url,
      body = Some(RequestBody(Json4sUtil.any2String(device).get, APPLICATION_JSON)),
      requestHeaders = authHeaders(oidcToken = oidcToken, ubirchToken = ubirchToken)
    )

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
