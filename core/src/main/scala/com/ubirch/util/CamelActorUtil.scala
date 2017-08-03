package com.ubirch.util

import com.ubirch.avatar.config.Config

trait CamelActorUtil {

  def sqsEndpoint(sqsQueueName: String): String = {
    s"aws-sqs://$sqsQueueName?region=${Config.awsRegion}&queueOwnerAWSAccountId=${Config.awsQueueOwnerId}&accessKey=${Config.awsAccessKey}&secretKey=${Config.awsSecretAccessKey}&concurrentConsumers=2"
  }

  def sqsEndpointConsumer(queue: String): String = {
    s"${sqsEndpoint(queue)}&maxMessagesPerPoll=${Config.awsSqsMaxMessagesPerPoll}"
  }

}



