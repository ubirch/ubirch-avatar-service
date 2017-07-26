package com.ubirch.avatar.client.rest

import java.net.URL

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.client.rest.config.AvatarClientConfig
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceInfo}
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.json.Json4sUtil

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.header.{Header, Headers}
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.{Response, Status}

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

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceUpdate}")
    logger.debug(s"try to call REST endpoint: $url")
    val msg = Json4sUtil.any2String(deviceDataRaw).get
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

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceBulk}")
    logger.debug(s"try to call REST endpoint: $url")
    val msg = Json4sUtil.any2String(deviceDataRaw).get
    val body = Some(RequestBody(msg, APPLICATION_JSON))
    val headers: Headers = new Headers(List(Header(name = "Authorization", value = s"Bearer $authToken")))

    httpClient.post(url, body, headers)

  }

  def devicePOST(authToken: String, device: Device): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDevice}")
    logger.debug(s"try to call REST endpoint: $url")
    val msg = Json4sUtil.any2String(device).get
    val body = Some(RequestBody(msg, APPLICATION_JSON))
    val headers: Headers = new Headers(List(Header(name = "Authorization", value = s"Bearer $authToken")))

    httpClient.post(url, body, headers)

  }

  /**
    * @param authToken token of the user whose device stubs will be listed
    * @return None in case of an error; other a sequence (empty if no devices are found)
    */
  def deviceStubGET(authToken: String): Option[Set[DeviceInfo]] = {

    val url = new URL(s"$baseUrl${RouteConstants.pathDeviceStub}")
    logger.debug(s"try to call REST endpoint: $url")
    val headers: Headers = new Headers(List(Header(name = "Authorization", value = s"Bearer $authToken")))

    val res = httpClient.get(url, headers)

    if (res.status == Status.S200_OK) {

      val jsonString = res.body.asString
      val deviceInfos = Json4sUtil.any2any[Set[DeviceInfo]](jsonString)
      Some(deviceInfos)

    } else {

      logger.error(s"failed to query device stubs: response=$res")
      None

    }

  }

}
