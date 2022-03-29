package com.ubirch.avatar.core.kafka


import akka.Done
import akka.actor.{ActorSystem, Scheduler}
import akka.event.Logging
import akka.event.slf4j.Logger
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.pattern.after
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.{ActorAttributes, Attributes, Materializer, Supervision}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.config.Config.KafkaRetryConfig
import com.ubirch.avatar.core.kafka.util.InvalidDataException
import com.ubirch.util.json.MyJsonProtocol
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

abstract class KafkaConsumer(topicNames: Set[String], groupName: String, actorSystem: ActorSystem)(
  implicit mat: Materializer)
  extends MyJsonProtocol {

  implicit val scheduler: Scheduler = actorSystem.scheduler

  private val logger = Logger.apply(this.getClass.getName)

  protected val consumerSettings: ConsumerSettings[String, String] = ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(Config.kafkaConBootstrapServers)
    .withGroupId(groupName)

  protected val committerSettings: CommitterSettings = CommitterSettings(actorSystem)

  protected def kafkaSource(): Source[CommittableMessage[String, String], Consumer.Control] =
    Consumer.committableSource(consumerSettings, Subscriptions.topics(topicNames))

  /**
    * This is a main logic of handling message from kafka
    *
    * @Important:
    * When calls an Actor in this function, use ask pattern as much as possible to apply back pressure.
    * Otherwise, it could lead to Out of Memory.
    */
  protected def handleMessage(message: String)(implicit ec: ExecutionContext): Future[Unit]

  /**
    * This is a retry handler
    * When an invalid data exception occurs in this function, this process is not proceeded again.
    * When an unexpected exception occurs in this function, this process is proceeded again with exponential backoff up to maxRetries.
    */
  private[kafka] def retryHandler(
                                   message: CommittableMessage[String, String],
                                   maxRetries: Int,
                                   backoffFactor: Double,
                                   initialBackoff: Int,
                                   maxBackoff: Int)(implicit ec: ExecutionContext, s: Scheduler): Future[Unit] = {
    val messageInfo =
      s"topic: ${message.record.topic}, group: $groupName, partition: ${message.committableOffset.partitionOffset.key}, offset: ${message.committableOffset.partitionOffset.offset}"

    def retry(currentDelay: Int, retriesLeft: Int): Future[Unit] = {
      handleMessage(message.record.value())
        .map { _ =>
          logger.info(s"succeeded to process message. $messageInfo")
        }
        .recoverWith {
          case InvalidDataException(err) =>
            logger.error(s"Invalid data exception occurred. Won't be retried. $err. $messageInfo")
            Future.successful(())
          case err =>
            if (retriesLeft > 0) {
              val nextDelay = Math.ceil(currentDelay * backoffFactor).toInt.min(maxBackoff)
              logger.warn(
                s"An unexpected exception occurred because of ${err.getMessage}, retry after ${currentDelay.millisecond}[ms]($retriesLeft retries left). $messageInfo")
              after(currentDelay.millisecond, s)(retry(nextDelay, retriesLeft - 1))
            } else {
              logger.error(
                s"Giving up after $maxRetries attempts because of ${err.getMessage}. this message will be committed. message: $message. $messageInfo")
              Future.successful(())
            }
        }
    }

    retry(initialBackoff, maxRetries)
  }

  /**
    * this is a supervision strategy for commit error.
    *
    * @Reference: https://github.com/akka/alpakka-kafka/issues/750
    */
  private val resumeOnCommitFailed: Supervision.Decider = {
    // Supervision.Resume means that skips commit of current message and process next message.
    case _: CommitFailedException =>
      logger.error("commit failed exception occurred. skip the commit of the current message. actor will resume.")
      Supervision.Resume
    case ex =>
      logger.error(s"unexpected error occurred. actor will stop. error $ex", ex)
      Supervision.Stop
  }

  /**
    * This method makes consumer running
    */
  def runWithRetry(kafkaRetryConfig: KafkaRetryConfig)(
    implicit executionContext: ExecutionContext): DrainingControl[Done] = {
    logger.info(s"$topicNames consumer starts running.")
    kafkaSource()
      .log(name = s"$topicNames consumer")
      .addAttributes(
        Attributes.logLevels(
          onElement = Attributes.LogLevels.Off,
          onFinish = Attributes.LogLevels.Off,
          onFailure = Logging.ErrorLevel
        ))
      .mapAsync(Config.kafkaSubscribeParallel) { msg =>
        val result =
            retryHandler(
              msg,
              kafkaRetryConfig.maxRetries,
              kafkaRetryConfig.backoffFactor,
              kafkaRetryConfig.minBackoff,
              kafkaRetryConfig.maxBackoff)
        result.map { _ =>
          msg.committableOffset
        }
      }
      .toMat(Committer.sink(committerSettings.withMaxBatch(Config.kafkaMaxCommit))
        .withAttributes(ActorAttributes.supervisionStrategy(resumeOnCommitFailed)))(Keep.both)
      .mapMaterializedValue((r: (Consumer.Control, Future[Done])) => DrainingControl.apply(r))
      .run()
  }

  private[kafka] def messageInfoFromOffsetBatch(offsetBatch: CommittableOffsetBatch): String = {
    offsetBatch.offsets.foldLeft("") { (m, offset) =>
      m + s"topic: ${offset._1.topic}, groupId: ${offset._1.groupId}, partition: ${offset._1.partition}, offset: ${offset._2}. "
    }
  }
}
