package com.ubirch.avatar.awsiot.services

import java.io.ByteArrayInputStream

import com.amazonaws.services.iotdata.model.GetThingShadowRequest
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.awsiot.config.AwsConf
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.aws.ThingShadowState
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.json4s._

/**
  * Created by derMicha on 04/04/16.
  */

object AwsShadowService extends MyJsonProtocol with LazyLogging {

  /**
    * Determines current AWS IoT thing state
    *
    * @param awsDeviceShadowId AWS IoT thing name, which is the AWS IoT Thing Id
    * @return current state as ThingShadowState
    */
  def getCurrentDeviceState(awsDeviceShadowId: String): ThingShadowState = {
    ThingShadowState(
      desired = getDesired(awsDeviceShadowId),
      reported = getReported(awsDeviceShadowId)
    )
  }

  /**
    * Determines current AWS IoT desired thing state
    * http://docs.aws.amazon.com/iot/latest/developerguide/using-thing-shadows.html#retrieving-thing-shadow
    *
    * @param awsDeviceShadowId AWS IoT thing name, which is the AWS IoT Thing Id
    * @return current desired state as Option[JValue]
    */
  def getDesired(awsDeviceShadowId: String): Option[JValue] = {
    getState(awsDeviceShadowId, Config.awsStatesDesired)
  }

  /**
    * Determines current AWS IoT reported thing state
    * http://docs.aws.amazon.com/iot/latest/developerguide/using-thing-shadows.html#retrieving-thing-shadow
    *
    * @param awsDeviceShadowId AWS IoT thing name, which is the AWS IoT Thing Id
    * @return current desired state as Option[JValue]
    */
  def getReported(awsDeviceShadowId: String): Option[JValue] = {
    getState(awsDeviceShadowId, Config.awsStatesReported)
  }

  private def getState(awsDeviceShadowId: String, state: String): Option[JValue] = {
    getShadowResource(awsDeviceShadowId) match {
      case Some(thingShadow) =>
        val inputStream = new ByteArrayInputStream(thingShadow.getPayload().array())
        //        val input: String = scala.io.Source.fromInputStream(inputStream).getLines().next()
        Json4sUtil.inputstream2jvalue(inputStream) match {
          //        Json4sUtil.string2JValue(input) match {
          case Some(jval) =>
            (jval \ "state" \ state).extractOpt[JValue]
          case None => None
        }
      case None =>
        None
    }
  }

  private def getShadowResource(awsDeviceShadowId: String) = {
    try {
      val awsIotDataClient = AwsConf.awsIotDataClient
      var getThingShadowRequest = new GetThingShadowRequest()
      getThingShadowRequest.setThingName(awsDeviceShadowId)
      Some(awsIotDataClient.getThingShadow(getThingShadowRequest))
    } catch {
      case e: Exception =>
        logger.error(s"error while accessing device shadow: $awsDeviceShadowId", e)
        None
    }

  }
}