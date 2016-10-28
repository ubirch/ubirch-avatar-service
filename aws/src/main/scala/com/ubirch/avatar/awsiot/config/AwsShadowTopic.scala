package com.ubirch.avatar.awsiot.config

import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 20/04/16.
  */
trait AwsShadowTopic {

  val deviceName: String

  private def getDeviceBaseTopic = {
    Config.awsTopicsBasename.format(deviceName.trim)
  }

  def getUpdateTopic = {
    s"$getDeviceBaseTopic/update"
  }

  def getUpdateAcceptedTopic = {
    s"$getUpdateTopic/accepted"
  }

  def getUpdateRejectedTopic = {
    s"$getUpdateTopic/rejected"
  }

  def getUpdateDeltaTopic = {
    s"$getUpdateTopic/delta"
  }

  def getGetTopic = {
    s"$getDeviceBaseTopic/get"
  }

  def getGetAcceptedTopic = {
    s"$getGetTopic/accepted"
  }

  def getGetRejectedTopic = {
    s"$getGetTopic/rejected"
  }

  def getDeleteTopic = {
    s"$getDeviceBaseTopic/delete"
  }

  def getDeleteAcceptedTopic = {
    s"$getDeleteTopic/accepted"
  }

  def getDeleteRejectedTopic = {
    s"$getDeleteTopic/rejected"
  }
}
