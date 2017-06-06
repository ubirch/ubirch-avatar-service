package com.ubirch.avatar.awsiot.services

import java.io.ByteArrayInputStream

import com.amazonaws.services.iotdata.model.GetThingShadowResult
import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.aws.ThingShadowState
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s._

/**
  * Created by derMicha on 04/04/16.
  */

object AwsShadowService extends MyJsonProtocol with StrictLogging {

  /**
    * Determines current AWS IoT thing state
    *
    * @param deviceId AWS IoT thing name, which is the AWS IoT Thing Id
    * @return current state as ThingShadowState
    */
  def getCurrentDeviceState(deviceId: String): Option[ThingShadowState] = {
    try {

      //      Some(ThingShadowState(
      //        inSync = getSyncState(awsDeviceShadowId),
      //        desired = getDesired(awsDeviceShadowId),
      //        reported = getReported(awsDeviceShadowId),
      //        delta = getDelta(awsDeviceShadowId),
      //        deviceLastUpdated = getTimestamp(awsDeviceShadowId),
      //        avatarLastUpdated = getTimestamp(awsDeviceShadowId)
      //      ))
      Some(ThingShadowState(inSync = Some(true)))
    }
    catch {
      case e: Exception => logger.error("could not get current shadow state", e)
        None
    }
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
    * @return current reported state as Option[JValue]
    */
  def getReported(awsDeviceShadowId: String): Option[JValue] = {
    getState(awsDeviceShadowId, Config.awsStatesReported)
  }

  /**
    * Determines current AWS IoT delta thing state
    * http://docs.aws.amazon.com/iot/latest/developerguide/using-thing-shadows.html#retrieving-thing-shadow
    *
    * @param awsDeviceShadowId AWS IoT thing name, which is the AWS IoT Thing Id
    * @return current delta state as Option[JValue]
    */
  def getDelta(awsDeviceShadowId: String): Option[JValue] = {
    getState(awsDeviceShadowId, Config.awsStatesDelta)
  }

  /**
    * Determines current AWS IoT delta thing state
    * http://docs.aws.amazon.com/iot/latest/developerguide/using-thing-shadows.html#retrieving-thing-shadow
    *
    * @param awsDeviceShadowId AWS IoT thing name, which is the AWS IoT Thing Id
    * @return current delta state as Option[JValue]
    */
  def getTimestamp(awsDeviceShadowId: String): Option[DateTime] = {
    getState(awsDeviceShadowId, Config.awsStatesTimestamp) match {
      case Some(jint) =>
        jint.extractOpt[Int] match {
          case Some(ts) =>
            val datezone = DateTimeZone.UTC
            val d = new DateTime(ts.toLong * 1000, datezone)
            Some(d)
          case None =>
            None
        }
      case None =>
        None
    }
  }

  private def getState(awsDeviceShadowId: String, state: String): Option[JValue] = {
    try {
      getShadowResource(awsDeviceShadowId) match {
        case Some(thingShadow) =>
          val inputStream = new ByteArrayInputStream(thingShadow.getPayload().array())
          //        val input: String = scala.io.Source.fromInputStream(inputStream).getLines().next()
          Json4sUtil.inputstream2jvalue(inputStream) match {
            //        Json4sUtil.string2JValue(input) match {
            case Some(jval) =>
              if (state == Config.awsStatesTimestamp)
                (jval \ state).extractOpt[JValue]
              else
                (jval \ "state" \ state).extractOpt[JValue]
            case None => None
          }
        case None =>
          None
      }
    }
    catch {
      case e: Throwable =>
        logger.error(s"got no current state: $awsDeviceShadowId", e)
        None
    }
  }

  def getSyncState(awsDeviceShadowId: String): Option[Boolean] = {
    getDelta(awsDeviceShadowId) match {
      case Some(delta) =>
        if (delta.children.size > 0)
          Some(false)
        else
          Some(true)
      case None =>
        None
    }
  }

  private def getShadowResource(awsDeviceShadowId: String): Option[GetThingShadowResult] = {
    //@TODO AWSIOT removed
    //    try {
    //      val awsIotDataClient = AwsConf.awsIotDataClient
    //      var getThingShadowRequest = new GetThingShadowRequest().withThingName(awsDeviceShadowId)
    //      Some(awsIotDataClient.getThingShadow(getThingShadowRequest))
    //    } catch {
    //      case e: Exception =>
    //        logger.error(s"error while accessing device shadow: $awsDeviceShadowId", e)
    //        None
    //    }
    None
  }
}