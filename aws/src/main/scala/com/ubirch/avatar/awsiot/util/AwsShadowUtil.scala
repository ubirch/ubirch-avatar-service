package com.ubirch.avatar.awsiot.util

import java.nio.ByteBuffer

import com.amazonaws.services.iot.model.{AttributePayload, CreateThingRequest, DeleteThingRequest, DeleteThingResult}
import com.amazonaws.services.iotdata.model.PublishRequest
import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.awsiot.config.AwsConf
import com.ubirch.avatar.model.rest.aws.{ThingShadowMessage, ThingShadowState}
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.avatar.model.rest.util.AwsThingTopicUtil
import com.ubirch.util.json.JsonFormats

import org.json4s._
import org.json4s.native.Serialization._

/**
  * Created by derMicha on 21/04/16.
  */
object AwsShadowUtil extends StrictLogging {

  private implicit val formats = JsonFormats.default

  private val iotDataClient = AwsConf.awsIotDataClient
  private val iotClient = AwsConf.awsIotClient

  def publish(topic: String, thingShadowMessage: ThingShadowMessage): Unit = {
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

    val pl = new AttributePayload()
    pl.addAttributesEntry("i", "900")

    createSensorRequest.setThingName(awsDeviceShadowId)
    createSensorRequest.withAttributePayload(pl)
    val response = iotClient.createThing(createSensorRequest)

    logger.debug(s"created shadow with name ${response.getThingName}")
    logger.debug(s"created shadow with arn ${response.getThingArn}")
    val thingName = response.getThingName

    //    val gtsr = new GetThingShadowRequest()
    //    gtsr.withThingName(thingName)
    //    val thing = iotDataClient.getThingShadow(gtsr)

    //    val awsDev = new AWSIotDevice(thingName)
    //    awsDev.activate()
    //    awsDev.update("{}")

    thingName
  }

  def deleteShadow(awsDeviceShadowId: String): DeleteThingResult = {
    logger.debug(s"delete shadow with id $awsDeviceShadowId")
    val deleteSensorRequest = new DeleteThingRequest()
    deleteSensorRequest.setThingName(awsDeviceShadowId)
    iotClient.deleteThing(deleteSensorRequest)
  }

  def setReported(device: Device, newState: JValue): Unit = {
    setReported(device.awsDeviceThingId, newState = newState)
  }

  def setReported(awsDeviceThingId: String, newState: JValue): Unit = {
    val thingShadowMessage = ThingShadowMessage(
      state =
        ThingShadowState(
          reported = Some(newState)
        )
    )
    publish(AwsThingTopicUtil.getUpdateTopic(awsDeviceThingId), thingShadowMessage)
  }

  def setDesired(device: Device, newState: JValue): Unit = {
    setDesired(device.awsDeviceThingId, newState = newState)
  }

  def setDesired(awsDeviceThingId: String, newState: JValue): Unit = {
    val thingShadowMessage = ThingShadowMessage(
      state =
        ThingShadowState(
          desired = Some(newState)
        )
    )
    publish(AwsThingTopicUtil.getUpdateTopic(awsDeviceThingId), thingShadowMessage)
  }
}