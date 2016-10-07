package com.ubirch.services.storage

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.util.json.Json4sUtil
import org.json4s._
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, Matchers}

/**
  * Created by derMicha on 06/10/16.
  */
class ElasticsearchStorageTest extends FeatureSpec
  with Matchers
  with BeforeAndAfterAll
  with LazyLogging {

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all

  val docIndex = "tests"

  val docType = "test"

  case class TestDoc(id:String, hello: String)

  val testDoc = TestDoc("1", "World")

  val testDoc2 = TestDoc("1", "Galaxy")

  feature("simple CRUD tests") {

    scenario("store") {

      val jval = Json4sUtil.any2jvalue(testDoc).get

      ElasticsearchStorage.storeDoc(docIndex, docType, testDoc.id, jval) match {
        case Some(rjval) =>
          val rTestDoc = rjval.extract[TestDoc]
          rTestDoc.hello shouldBe testDoc.hello
        case None =>
          fail("could not parse stored document")
      }
    }

    scenario("get") {
      ElasticsearchStorage.getDoc(docIndex, docType, testDoc.id) match {
        case Some(jval) =>
          val rTestDoc = jval.extract[TestDoc]
          rTestDoc.id shouldBe testDoc.id
          rTestDoc.hello shouldBe testDoc.hello
        case _ => fail("could not reag stored document")
      }
    }

    scenario("update") {
      val jval = Json4sUtil.any2jvalue(testDoc2).get
      ElasticsearchStorage.storeDoc(docIndex, docType, testDoc2.id, jval)
      ElasticsearchStorage.getDoc(docIndex, docType, testDoc2.id) match {
        case Some(jval) =>
          val rTestDoc = jval.extract[TestDoc]
          rTestDoc.id shouldBe testDoc2.id
          rTestDoc.hello shouldBe testDoc2.hello
        case _ => fail("could not reag stored document")
      }
    }

//    scenario("getAll") {
//
//      ElasticsearchStorage.getDocs(docIndex, docType) match {
//        case Some(jval) =>
//          val rTestDoc = jval.extract[TestDoc]
//          rTestDoc.id shouldBe testDoc.id
//          rTestDoc.hello shouldBe testDoc.hello
//        case _ => fail("could not reag stored document")
//      }
//    }

    scenario("delete") {
      ElasticsearchStorage.deleteDoc(docIndex, docType, testDoc.id) shouldBe true
    }
  }
}