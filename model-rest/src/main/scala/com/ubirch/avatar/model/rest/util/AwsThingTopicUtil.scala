package com.ubirch.avatar.model.rest.util

/**
  * Created by derMicha on 20/04/16.
  */
object AwsThingTopicUtil {

  private def getDeviceBaseTopic(awsThingName: String) = {
    "$aws/things/%s/shadow".format(awsThingName.trim)
  }

  def getUpdateTopic(awsThingName: String): String = {
    s"${getDeviceBaseTopic(awsThingName: String)}/update"
  }

  def getUpdateAcceptedTopic(awsThingName: String): String = {
    s"${getUpdateTopic(awsThingName: String)}/accepted"
  }

  def getUpdateRejectedTopic(awsThingName: String): String = {
    s"${getUpdateTopic(awsThingName: String)}/rejected"
  }

  def getUpdateDeltaTopic(awsThingName: String): String = {
    s"${getUpdateTopic(awsThingName: String)}/delta"
  }

  def getGetTopic(awsThingName: String): String = {
    s"${getDeviceBaseTopic(awsThingName: String)}/get"
  }

  def getGetAcceptedTopic(awsThingName: String): String = {
    s"${getGetTopic(awsThingName: String)}/accepted"
  }

  def getGetRejectedTopic(awsThingName: String): String = {
    s"${getGetTopic(awsThingName: String)}/rejected"
  }

  def getDeleteTopic(awsThingName: String): String = {
    s"${getDeviceBaseTopic(awsThingName: String)}/delete"
  }

  def getDeleteAcceptedTopic(awsThingName: String): String = {
    s"${getDeleteTopic(awsThingName: String)}/accepted"
  }

  def getDeleteRejectedTopic(awsThingName: String): String = {
    s"${getDeleteTopic(awsThingName: String)}/rejected"
  }
}
