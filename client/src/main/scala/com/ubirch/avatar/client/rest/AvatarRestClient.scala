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

  val httpClient = new HttpClient

  def esRestUrl = s"${Config.esProtocol}${Config.esHost}:${Config.esPortHttp}"

  def deviceUpdate(deviceDataRaw: DeviceDataRaw): Response = {

    val url = new URL(s"$esRestUrl${RouteConstants.urlDeviceUpdate}")
    val msg = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(deviceDataRaw).get)
    logger.info(s"msg: $msg")
    val body = Some(RequestBody(msg, APPLICATION_JSON))

    httpClient.post(url, body)

  }

}
