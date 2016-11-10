package com.ubirch.avatar.awsiot.util

import java.nio.ByteBuffer

import com.amazonaws.services.iot.model.{CreateThingRequest, DeleteThingRequest}
import com.amazonaws.services.iotdata.model.PublishRequest
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.awsiot.config.AwsConf
import com.ubirch.avatar.model.aws.ThingShadowMessage
import com.ubirch.util.json.JsonFormats
import org.json4s.native.Serialization._

/**
  * Created by derMicha on 21/04/16.
  */
object AwsShadowUtil extends LazyLogging {

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

  def createShadow(awsDeviceShadowId: String) = {
    logger.debug(s"create shadow with id $awsDeviceShadowId")
    val createSensorRequest = new CreateThingRequest()
    createSensorRequest.setThingName(awsDeviceShadowId)
    iotClient.createThing(createSensorRequest)
  }

  def deleteShadow(awsDeviceShadowId: String) = {
    logger.debug(s"delete shadow with id $awsDeviceShadowId")
    val deleteSensorRequest = new DeleteThingRequest()
    deleteSensorRequest.setThingName(awsDeviceShadowId)
    iotClient.deleteThing(deleteSensorRequest)
  }
}