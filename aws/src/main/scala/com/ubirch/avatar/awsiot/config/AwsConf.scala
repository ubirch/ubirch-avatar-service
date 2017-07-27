package com.ubirch.avatar.awsiot.config

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSClient
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config

/**
  * Created by derMicha on 04/04/16.
  */
object AwsConf extends StrictLogging {

  val region: String = "us-east-1"

  val accessKey = Config.awsAccessKey

  val secretKey = Config.awsSecretAccessKey

  val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)

  lazy val awsSqsClient: AmazonSQSClient = {

    Config.awsLocalMode match {
      case true =>
        //        val cred = new ProfileCredentialsProvider()
        val cred = new BasicAWSCredentials(accessKey, secretKey)
        new AmazonSQSClient(cred)
      case _ => new
          AmazonSQSClient()
    }
  }
}
