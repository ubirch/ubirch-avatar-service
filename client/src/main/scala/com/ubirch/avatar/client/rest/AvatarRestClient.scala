package com.ubirch.avatar.client.rest

import java.net.URL

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.server.util.RouteConstants
import com.ubirch.util.json.Json4sUtil

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.Response

/**
  * author: cvandrei
  * since: 2016-11-15
  */
object AvatarRestClient extends StrictLogging {

  private val httpClient = new HttpClient(commonConfig = uk.co.bigbeeconsultants.http.Config(
    connectTimeout = Config.restClientTimeoutConnect,
    readTimeout = Config.restClientTimeoutRead
  ))

  private def baseUrl = s"${Config.protocol}${Config.interface}:${Config.port}"

  def deviceUpdate(deviceDataRaw: DeviceDataRaw): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.urlDeviceUpdate}")
    val msg = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(deviceDataRaw).get)
    logger.info(s"msg: $msg")
    val body = Some(RequestBody(msg, APPLICATION_JSON))

    httpClient.post(url, body)

  }

  def deviceBulk(deviceDataRaw: DeviceDataRaw): Response = {

    val url = new URL(s"$baseUrl${RouteConstants.urlDeviceBulk}")
    val msg = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(deviceDataRaw).get)
    logger.info(s"msg: $msg")
    val body = Some(RequestBody(msg, APPLICATION_JSON))

    httpClient.post(url, body)

  }

}
