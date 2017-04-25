package com.ubirch.avatar.core.actor

import com.ubirch.avatar.core.device.DeviceDataRawAnchoredManager
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.avatar.test.base.ElasticsearchSpec

import akka.actor.ActorSystem
import akka.testkit.TestActorRef

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-02-23
  */
class MessageNotaryActorSpec extends ElasticsearchSpec {

  feature("receive DeviceDataRaw") {

    scenario("incoming DeviceDataRaw has no txHash; is written to Elasticsearch") {

      // prepare
      implicit val system = ActorSystem()
      val notaryActor = TestActorRef(new MessageNotaryActor)

      val device = DummyDevices.minimalDevice()
      val rawBefore = DummyDeviceDataRaw.data(device = device)()

      // test
      notaryActor ! rawBefore

      // verify
      Thread.sleep(4000)

      val rawAfterOpt = Await.result(DeviceDataRawAnchoredManager.byId(rawBefore.id), 3 seconds)
      rawAfterOpt should be('isDefined)

      val rawAfter = rawAfterOpt.get
      rawAfter.ch should be('isDefined)
      rawAfter.txHash should be('isDefined)
      rawAfter.txHashLink should be('isDefined)
      rawAfter.txHashLinkHtml should be('isDefined)
      rawAfter.copy(ch = None, txHash = None, txHashLink = None, txHashLinkHtml = None) should be(rawBefore)

    }

    scenario("incoming DeviceDataRaw has a txHash and txHashLink (which are ignored); is written to Elasticsearch") {

      // prepare
      implicit val system = ActorSystem()
      val notaryActor = TestActorRef(new MessageNotaryActor)

      val device = DummyDevices.minimalDevice()
      val rawBefore = DummyDeviceDataRaw.data(device = device)().copy(txHash = Some("foo"), txHashLink = Some("http://example.com/foo"))

      // test
      notaryActor ! rawBefore

      // verify
      Thread.sleep(4000)

      val rawAfterOpt = Await.result(DeviceDataRawAnchoredManager.byId(rawBefore.id), 3 seconds)
      rawAfterOpt should be('isDefined)
      val rawAfter = rawAfterOpt.get

      rawAfter.ch should be('isDefined)
      rawAfter.txHash should be('isDefined)
      rawAfter.txHashLink should be('isDefined)
      rawAfter.txHashLinkHtml should be('isDefined)
      rawAfter.txHash should not be rawBefore.txHash
      rawAfter.copy(ch = None, txHash = None, txHashLink = None, txHashLinkHtml = None) should be(rawBefore.copy(txHash = None))

    }

  }

}
