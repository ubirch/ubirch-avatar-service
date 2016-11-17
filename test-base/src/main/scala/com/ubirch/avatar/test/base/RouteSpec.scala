package com.ubirch.avatar.test.base

import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.test.cors.CORSUtil

import akka.http.scaladsl.testkit.RouteTestTimeout

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait RouteSpec extends UnitSpec
  with CORSUtil
  with MyJsonProtocol {

  implicit val timeout = RouteTestTimeout(10 seconds)

}
