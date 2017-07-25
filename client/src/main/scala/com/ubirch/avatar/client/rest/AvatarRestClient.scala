package com.ubirch.avatar.client.rest

import java.net.URL

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.client.rest.config.AvatarClientConfig
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.json.Json4sUtil

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.{Header, Headers}
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.Response

/**
  * author: cvandrei
  * since: 2016-11-15
  */
object AvatarRestClient extends StrictLogging {

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

    val path = RouteConstants.pathDeviceUpdate
    val url = new URL(s"$baseUrl$path")
    logger.debug(s"try to call REST endpoint: $url")
    val msg = Json4sUtil.any2String(deviceDataRaw).get
    logger.info(s"POST $path: body=$msg")
    val body = Some(RequestBody(msg, APPLICATION_JSON))

    httpClient.post(url, body)

  }

  /**
    * Bulk update a device by POSTing raw device data.
    *
    * @param deviceDataRaw raw data to POST
    * @return http response
    */
  def deviceBulkPOST(authToken: String, deviceDataRaw: DeviceDataRaw): Response = {

    val path = RouteConstants.pathDeviceBulk
    val url = new URL(s"$baseUrl$path")
    logger.debug(s"try to call REST endpoint: $url")
    val msg = Json4sUtil.any2String(deviceDataRaw).get
    logger.info(s"POST $path: body=$msg")
    val body = Some(RequestBody(msg, APPLICATION_JSON))
    val headers: Headers = new Headers(List(Header(name = "Authorization", value = s"Bearer $authToken")))

    httpClient.post(url, body, headers)

  }

  def devicePOST(authToken: String, device: Device): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDevice}")
    logger.debug(s"try to call REST endpoint: $url")
    val msg = Json4sUtil.any2String(device).get
    logger.info(s"POST ${RouteConstants.pathDevice}: body=$msg")
    val body = Some(RequestBody(msg, APPLICATION_JSON))
    val headers: Headers = new Headers(List(Header(name = "Authorization", value = s"Bearer $authToken")))

    httpClient.post(url, body, headers)

  }

}
