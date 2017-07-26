package com.ubirch.avatar.test.base

import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.test.cors.CORSUtil

import org.scalatest.BeforeAndAfterAll

import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.http.scaladsl.{Http, HttpExt}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait RouteSpec extends UnitSpec
  with BeforeAndAfterAll
  with CORSUtil
  with MyJsonProtocol {

  implicit val httpClient: HttpExt = Http()
  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  implicit val timeout = RouteTestTimeout(10 seconds)

}
