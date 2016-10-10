package com.ubirch.avatar.core.elasticsearch

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, RequestEntity}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.Predef._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.Try

/**
  * author: cvandrei
  * since: 2016-09-29
  */
// TODO extract ESClient to ubirch-scala-utils project
class ESClient(val host: String,
               val port: Int,
               val protocol: String = "https",
               systemIn: ActorSystem = ActorSystem(),
               materializerIn: Option[ActorMaterializer] = None
              ) {

  implicit val system = systemIn
  implicit val materializer = materializerIn match {
    case None => ActorMaterializer()
    case Some(someMaterializer) => someMaterializer
  }

  private val connectionPool: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), HostConnectionPool] = {
    // TODO pool should be configurable
    protocol match {
      case "https" => Http().cachedHostConnectionPoolHttps(host, port)
      case _ => Http().cachedHostConnectionPool[Int](host, port)
    }
  }

  def get(index: String, esType: String, id: String): Future[(Try[HttpResponse], Int)] = {
    val uri = s"/$index/$esType/$id"
    val req = HttpRequest(uri = uri, method = GET)
    call(req)
  }

  def search(indexSet: Set[String], typeSet: Set[String], query: String): Future[(Try[HttpResponse], Int)] = {

    assert(query != "")

    val uri = ESClientUtil.searchPath(indexSet, typeSet)
    val req = HttpRequest(uri = uri, entity = jsonEntity(query), method = GET)

    call(req)

  }

  def insert(index: String, esType: String, json: String): Future[(Try[HttpResponse], Int)] = {

    assert(index != "")
    assert(esType != "")
    assert(json != "")

    val uri = s"$index/$esType"
    val req = HttpRequest(uri = uri, entity = jsonEntity(json), method = POST)

    call(req)

  }

  def update(index: String, esType: String, id: String, json: String) = {

    assert(index != "")
    assert(esType != "")
    assert(id != "")
    assert(json != "")

    val uri = s"$index/$esType/$id"
    val req = HttpRequest(uri = uri, entity = jsonEntity(json), method = POST)

    call(req)

  }

  def upsert(index: String, esType: String, id: String, json: String) = {

    assert(index != "")
    assert(esType != "")
    assert(id != "")
    assert(json != "")

    val uri = s"$index/$esType/$id"
    val req = HttpRequest(uri = uri, entity = jsonEntity(json), method = PUT)

    call(req)

  }

  def delete(index: String, esType: String, id: String): Future[(Try[HttpResponse], Int)] = {

    assert(index != "")
    assert(esType != "")
    assert(id != "")

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

  private def jsonEntity(data: String): RequestEntity = HttpEntity(`application/json`, data)

  private def callSimple(req: HttpRequest): Future[HttpResponse] = Http().singleRequest(request = req)

}
