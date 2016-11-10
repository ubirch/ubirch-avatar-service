package com.ubirch.avatar.model.util

/**
  * Created by derMicha on 20/04/16.
  */
object AwsThingTopicUtil {

  private def getDeviceBaseTopic(awsThingName: String) = {
    "$aws/things/%s/shadow".format(awsThingName.trim)
  }

  def getUpdateTopic(awsThingName: String) = {
    s"${getDeviceBaseTopic(awsThingName: String)}/update"
  }

  def getUpdateAcceptedTopic(awsThingName: String) = {
    s"${getUpdateTopic(awsThingName: String)}/accepted"
  }

  def getUpdateRejectedTopic(awsThingName: String) = {
    s"${getUpdateTopic(awsThingName: String)}/rejected"
  }

  def getUpdateDeltaTopic(awsThingName: String) = {
    s"${getUpdateTopic(awsThingName: String)}/delta"
  }

  def getGetTopic(awsThingName: String) = {
    s"${getDeviceBaseTopic(awsThingName: String)}/get"
  }

  def getGetAcceptedTopic(awsThingName: String) = {
    s"${getGetTopic(awsThingName: String)}/accepted"
  }

  def getGetRejectedTopic(awsThingName: String) = {
    s"${getGetTopic(awsThingName: String)}/rejected"
  }

  def getDeleteTopic(awsThingName: String) = {
    s"${getDeviceBaseTopic(awsThingName: String)}/delete"
  }

  def getDeleteAcceptedTopic(awsThingName: String) = {
    s"${getDeleteTopic(awsThingName: String)}/accepted"
  }

  def getDeleteRejectedTopic(awsThingName: String) = {
    s"${getDeleteTopic(awsThingName: String)}/rejected"
  }
}
