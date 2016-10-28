package com.ubirch.avatar.awsiot.util

import java.nio.ByteBuffer

import com.amazonaws.services.iot.model.{CreateThingRequest, DeleteThingRequest}
import com.amazonaws.services.iotdata.AWSIotDataClient
import com.amazonaws.services.iotdata.model.PublishRequest
import com.ubirch.avatar.awsiot.config.AwsConf
import com.ubirch.avatar.model.aws.ThingShadowMessage
import org.json4s.native.Serialization._
import org.json4s.{NoTypeHints, jackson}

/**
  * Created by derMicha on 21/04/16.
  */
object AwsShadowUtil {

  implicit val formats = jackson.Serialization.formats(NoTypeHints) ++ org.json4s.ext.JodaTimeSerializers.all

  private val iotDataClient = new AWSIotDataClient()
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
    val createSensorRequest = new CreateThingRequest()
    createSensorRequest.setThingName(awsDeviceShadowId)
    iotClient.createThing(createSensorRequest)
  }

  def deleteShadow(awsDeviceShadowId: String) = {
    val deleteSensorRequest = new DeleteThingRequest()
    deleteSensorRequest.setThingName(awsDeviceShadowId)
    iotClient.deleteThing(deleteSensorRequest)
  }
}