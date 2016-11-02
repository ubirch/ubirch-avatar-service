package com.ubirch.avatar.awsiot.config

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iotdata.AWSIotDataClient
import com.amazonaws.services.sqs.AmazonSQSClient
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 04/04/16.
  */
object AwsConf extends LazyLogging {

  val region: String = "us-east-1"

  val accessKey = System.getenv().get("AWS_ACCESS_KEY_ID")

  val secretKey = System.getenv().get("AWS_SECRET_ACCESS_KEY")

  val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)

  lazy val awsIotDataClient: AWSIotDataClient = {
    Config.awsLocalMode match {
      case true =>
        val pcp = new ProfileCredentialsProvider()
        new AWSIotDataClient(pcp)
      case _ =>
        new AWSIotDataClient()
    }
  }

  lazy val awsIotClient: AWSIotClient = {

    Config.awsLocalMode match {
      case true =>
        new AWSIotClient(new ProfileCredentialsProvider())
      case _ => new
          AWSIotClient()
    }
  }

  lazy val awsSqsClient: AmazonSQSClient = {

    Config.awsLocalMode match {
      case true =>
        new AmazonSQSClient(new ProfileCredentialsProvider())
      case _ => new
          AmazonSQSClient()
    }
  }
}
