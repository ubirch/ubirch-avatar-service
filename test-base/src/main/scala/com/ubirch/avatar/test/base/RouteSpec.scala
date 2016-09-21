package com.ubirch.avatar.test.base

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.ubirch.util.json.MyJsonProtocol

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait RouteSpec extends UnitSpec
  with ScalatestRouteTest
  with MyJsonProtocol {

  implicit val timeout = RouteTestTimeout(10 seconds)

}
