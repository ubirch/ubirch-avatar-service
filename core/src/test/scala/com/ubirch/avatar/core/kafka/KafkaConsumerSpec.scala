package com.ubirch.avatar.core.kafka

import akka.Done
import akka.actor.{ActorSystem, Scheduler}
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.testkit.ConsumerResultFactory
import akka.kafka.testkit.scaladsl.ConsumerControlFactory
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config.KafkaRetryConfig
import com.ubirch.avatar.core.kafka.util.{InvalidDataException, UnexpectedException}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{AsyncFeatureSpec, Matchers}

import scala.collection.immutable._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.jdk.CollectionConverters._

class KafkaConsumerSpec extends AsyncFeatureSpec with ScalaFutures with StrictLogging with Matchers {
  implicit private val actorSystem: ActorSystem = ActorSystem("KafkaConsumerSpec")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit private val scheduler: Scheduler = actorSystem.scheduler
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  val topic = "test"
  val consumerRecord = new ConsumerRecord[String, String](topic, 1, 0, "key", "value")

  val partition = 2
  val nextOffset = 1
  val groupId = "test-group"
  val committableMessage: CommittableMessage[String, String] = ConsumerResultFactory.committableMessage(
    consumerRecord,
    ConsumerResultFactory.committableOffset(groupId, topic, partition, nextOffset, s"metadata")
  )
  val message = "test-message"
  val retries = 3
  val retryFactor = 2.0
  val initialDelay = 100
  val maxDelay = 10000

  feature("retryHandler") {
    scenario("handle is success --> Success(CommittableOffset)") {
      val kafkaConsumer = new KafkaConsumer(Set(), "test", actorSystem) {

        override protected def handleMessage(message: String)(implicit ec: ExecutionContext): Future[Unit] =
          Future.successful(())
      }

      val r = kafkaConsumer.retryHandler(committableMessage, retries, retryFactor, initialDelay, maxDelay)
      whenReady(r) { result =>
        assert(result.isInstanceOf[Unit])
      }
    }

    scenario("InvalidDataException occurs --> Success(CommittableOffset)") {
      val kafkaConsumer = new KafkaConsumer(Set(), "test", actorSystem) {

        override protected def handleMessage(message: String)(implicit ec: ExecutionContext): Future[Unit] =
          Future.failed(
            InvalidDataException("invalid data")
          )
      }

      val r = kafkaConsumer.retryHandler(committableMessage, retries, retryFactor, initialDelay, maxDelay)
      whenReady(r) { result =>
        assert(result.isInstanceOf[Unit])
      }
    }

    scenario(s"UnexpectedException occurs --> Success(CommittableOffset) after retry with $retries count") {
      val kafkaConsumer = new KafkaConsumer(Set(), "test", actorSystem) {

        override protected def handleMessage(message: String)(implicit ec: ExecutionContext): Future[Unit] =
          Future.failed(
            UnexpectedException("unexpected")
          )
      }

      val r = kafkaConsumer.retryHandler(committableMessage, retries, retryFactor, initialDelay, maxDelay)
      whenReady(r, timeout(Span(20, Seconds))) { result =>
        assert(result.isInstanceOf[Unit])
      }
    }

    scenario(s"RuntimeException occurs --> Success(CommittableOffset) after retry with $retries count") {
      val kafkaConsumer = new KafkaConsumer(Set(), "test", actorSystem) {

        override protected def handleMessage(message: String)(implicit ec: ExecutionContext): Future[Unit] =
          Future.failed(
            new RuntimeException("runtime exception")
          )
      }

      val r = kafkaConsumer.retryHandler(committableMessage, retries, retryFactor, initialDelay, maxDelay)
      whenReady(r, timeout(Span(20, Seconds))) { result =>
        assert(result.isInstanceOf[Unit])
      }
    }
  }

  feature("runWithRetry") {
    class KafkaConsumerMock extends KafkaConsumer(Set(), "test", actorSystem) {

      val topic = "test-topic"
      val groupId = "test-group"
      val partition = 2
      val elements: Iterable[CommittableMessage[String, String]] = (0 to 10).map { i =>
        val nextOffset = i
        logger.info(s"offset: $i")
        ConsumerResultFactory.committableMessage(
          new ConsumerRecord(topic, partition, nextOffset, "key", s"value $i"),
          ConsumerResultFactory.committableOffset(groupId, topic, partition, nextOffset, s"metadata $i")
        )
      }

      override protected def handleMessage(message: String)(implicit ec: ExecutionContext): Future[Unit] =
        Future.successful(())

      override protected def kafkaSource()
      : Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] =
        Source(elements).viaMat(ConsumerControlFactory.controlFlow())(Keep.right)
    }

    val kafkaRetryConfig = KafkaRetryConfig(100, 1000, 0.2, 3)
    scenario("consume properly") {
      val kafkaConsumer = new KafkaConsumerMock

      val c = kafkaConsumer.runWithRetry(kafkaRetryConfig)

      Thread.sleep(1000)
      whenReady(c.drainAndShutdown()) { r =>
        r shouldBe Done
      }
    }
  }

  feature("messageInfoFromOffsetBatch") {
    val kafkaConsumer = new KafkaConsumer(Set(), "test", actorSystem) {

      override protected def handleMessage(message: String)(implicit ec: ExecutionContext): Future[Unit] =
        Future.successful(())
    }

    scenario("single commit offset info") {
      val topic = "test"
      val partition = 2
      val nextOffset = 1
      val groupId = "test-group"
      val offset = ConsumerResultFactory.committableOffset(groupId, topic, partition, nextOffset, s"metadata")
      val offsetBatch = ConsumerMessage.createCommittableOffsetBatch(offset)
      val messageInfo = s"topic: $topic, groupId: $groupId, partition: $partition, offset: $nextOffset. "
      val result = kafkaConsumer.messageInfoFromOffsetBatch(offsetBatch)
      assert(result == messageInfo)
    }

    scenario("multi commits offset info") {
      val topic = "test"
      val partition = 2
      val nextOffset = 1
      val groupId = "test-group"
      val offset = ConsumerResultFactory.committableOffset(groupId, topic, partition, nextOffset, s"metadata")
      val offset2 = ConsumerResultFactory.committableOffset(groupId, topic, partition + 1, nextOffset, s"metadata")
      val offsetBatch = ConsumerMessage.createCommittableOffsetBatch(List(offset, offset2).asJava)
      val messageInfo =
        s"topic: $topic, groupId: $groupId, partition: $partition, offset: $nextOffset. topic: $topic, groupId: $groupId, partition: ${partition + 1}, offset: $nextOffset. "
      val result = kafkaConsumer.messageInfoFromOffsetBatch(offsetBatch)
      assert(result == messageInfo)
    }
  }
}
