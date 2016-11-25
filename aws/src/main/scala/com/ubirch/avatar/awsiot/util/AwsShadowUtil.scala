package com.ubirch.avatar.awsiot.util

import java.nio.ByteBuffer

import com.amazonaws.services.iot.model.{CreateThingRequest, DeleteThingRequest}
import com.amazonaws.services.iotdata.model.PublishRequest
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.awsiot.config.AwsConf
import com.ubirch.avatar.model.aws.{ThingShadowMessage, ThingShadowState}
import com.ubirch.avatar.model.device.Device
import com.ubirch.avatar.model.util.AwsThingTopicUtil
import com.ubirch.util.json.JsonFormats
import org.json4s._
import org.json4s.native.Serialization._

/**
  * Created by derMicha on 21/04/16.
  */
object AwsShadowUtil extends StrictLogging {

  implicit val formats = JsonFormats.default

  private val iotDataClient = AwsConf.awsIotDataClient
  private val iotClient = AwsConf.awsIotClient

  def publish(topic: String, thingShadowMessage: ThingShadowMessage) {
    val republishRequest = new PublishRequest()
      .withTopic(topic)
      .withPayload(ByteBuffer.wrap(write(thingShadowMessage).getBytes("UTF-8")))
    iotDataClient.publish(republishRequest)
  }

  //  def subscribe(topic: String) {
  //    val republishRequest = new SubscribeR
  //      .withTopic(topic)
  //      .withPayload(ByteBuffer.wrap(write(thingShadowMessage).getBytes("UTF-8")))
  //    iotDataClient.publish(republishRequest)
  //  }

  def createShadow(awsDeviceShadowId: String): String = {
    logger.debug(s"create shadow with id $awsDeviceShadowId")
    val createSensorRequest = new CreateThingRequest()

    createSensorRequest.setThingName(awsDeviceShadowId)

    val response = iotClient.createThing(createSensorRequest)
    logger.debug(s"created shadow with name ${response.getThingName}")
    logger.debug(s"created shadow with arn ${response.getThingArn}")
    response.getThingName
  }

  def deleteShadow(awsDeviceShadowId: String) = {
    logger.debug(s"delete shadow with id $awsDeviceShadowId")
    val deleteSensorRequest = new DeleteThingRequest()
    deleteSensorRequest.setThingName(awsDeviceShadowId)
    iotClient.deleteThing(deleteSensorRequest)
  }

  def setReported(device: Device, newState: JValue) = {
    val thingShadowMessage = ThingShadowMessage(
      state =
        ThingShadowState(
          reported = Some(newState)
        )
    )
    publish(AwsThingTopicUtil.getUpdateTopic(device.awsDeviceThingId), thingShadowMessage)
  }

  def setDesired(device: Device, newState: JValue) = {
    val thingShadowMessage = ThingShadowMessage(
      state =
        ThingShadowState(
          desired = Some(newState)
        )
    )
    publish(AwsThingTopicUtil.getUpdateTopic(device.awsDeviceThingId), thingShadowMessage)
  }
}