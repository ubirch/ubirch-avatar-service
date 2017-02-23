package com.ubirch.avatar.core.actor

import com.ubirch.avatar.core.device.DeviceDataRawAnchoredManager
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.avatar.test.base.ElasticsearchSpec

import akka.actor.ActorSystem
import akka.testkit.TestActorRef

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

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
      Thread.sleep(3000)
      val rawAfterOpt = Await.result(DeviceDataRawAnchoredManager.byId(rawBefore.id), 3 seconds)
      rawAfterOpt should be('isDefined)
      val rawAfter = rawAfterOpt.get
      rawAfter.txHash should be('isDefined)
      rawAfter.copy(txHash = None) should be(rawBefore)

    }

    scenario("incoming DeviceDataRaw has a txHash (which is ignored); is written to Elasticsearch") {

      // prepare
      implicit val system = ActorSystem()
      val notaryActor = TestActorRef(new MessageNotaryActor)

      val device = DummyDevices.minimalDevice()
      val rawBefore = DummyDeviceDataRaw.data(device = device)().copy(txHash = Some("foo"))

      // test
      notaryActor ! rawBefore

      // verify
      Thread.sleep(3000)
      val rawAfterOpt = Await.result(DeviceDataRawAnchoredManager.byId(rawBefore.id), 3 seconds)
      rawAfterOpt should be('isDefined)
      val rawAfter = rawAfterOpt.get
      rawAfter.txHash should be('isDefined)
      rawAfter.txHash should not be rawBefore.txHash
      rawAfter.copy(txHash = None) should be(rawBefore.copy(txHash = None))

    }

  }

}
