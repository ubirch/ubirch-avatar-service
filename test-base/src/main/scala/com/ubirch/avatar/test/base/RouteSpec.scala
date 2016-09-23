package com.ubirch.avatar.test.base

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, HttpOriginRange, `Access-Control-Allow-Origin`}
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

  def verifyCORSHeader(): Unit = {
    header("Access-Control-Allow-Origin") should be(Some(`Access-Control-Allow-Origin`(HttpOriginRange.*)))
    header("Access-Control-Allow-Methods") should be(Some(`Access-Control-Allow-Methods`(GET, POST, PUT, DELETE, OPTIONS)))
    header("Access-Control-Allow-Headers") should be(Some(`Access-Control-Allow-Headers`("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent")))
    header("Access-Control-Allow-Credentials") should be(Some(`Access-Control-Allow-Credentials`(true)))
  }

}
