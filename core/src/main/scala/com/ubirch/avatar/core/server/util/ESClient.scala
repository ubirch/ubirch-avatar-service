package com.ubirch.avatar.core.server.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, RequestEntity}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.ubirch.avatar.config.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.Try

/**
  * author: cvandrei
  * since: 2016-09-29
  */
object ESClient {

  // TODO extract ESClient to ubirch-scala-utils project

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val connectionPool: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), HostConnectionPool] = {

    // TODO pool should be configurable
    Config.esProtocol match {
      case "https" => Http().cachedHostConnectionPoolHttps(Config.esHost, Config.esPort)
      case _ => Http().cachedHostConnectionPool[Int](Config.esHost, Config.esPort)
    }

  }

  def get(index: String, esType: String, id: String): Future[(Try[HttpResponse], Int)] = {
    val uri = s"/$index/$esType/$id"
    val req = HttpRequest(uri = uri, method = GET)
    call(req)
  }

  def insert(index: String, esType: String, id: String, entity: RequestEntity): Future[(Try[HttpResponse], Int)] = {
    val uri = s"$index/$esType"
//    val entity = HttpEntity(`application/json`, data)
    val req = HttpRequest(uri = uri, entity = entity, method = POST)
    call(req)
  }

  def update(index: String, esType: String, id: String, entity: RequestEntity) = {
    val uri = s"$index/$esType/$id"
    val req = HttpRequest(uri = uri, entity = entity, method = POST)
    call(req)
  }

  def upsert(index: String, esType: String, id: String, entity: RequestEntity) = {
    val uri = s"$index/$esType/$id"
    val req = HttpRequest(uri = uri, entity = entity, method = PUT)
    call(req)
  }

  def delete(index: String, esType: String, id: String): Future[(Try[HttpResponse], Int)] = {
    val uri = s"/$index/$esType/$id"
    val req = HttpRequest(uri = uri, method = DELETE)
    call(req)
  }

  private def call(req: HttpRequest): Future[(Try[HttpResponse], Int)] = {
    // TODO add authentication
    Source.single(req -> 4)
      .via(connectionPool)
      .runWith(Sink.head)
  }

  def shutdown() = {
    Http().shutdownAllConnectionPools().onComplete { _ =>
      Await.result(system.terminate(), 20 seconds)
    }
  }

}
