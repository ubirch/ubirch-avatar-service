package com.ubirch.avatar.awsiot.config

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iotdata.AWSIotDataClient
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 04/04/16.
  */
object AwsConf extends LazyLogging {

  lazy val awsIotDataClient: AWSIotDataClient = {
    Config.awsLocalMode match {
      case true =>
        new AWSIotDataClient(new ProfileCredentialsProvider())
      case _ =>
        new AWSIotDataClient()
    }
  }

  lazy val awsIotClient: AWSIotClient = {
    Config.awsLocalMode match {
      case true =>
        new AWSIotClient(new ProfileCredentialsProvider())
      case _ => new AWSIotClient()
    }
  }
}
