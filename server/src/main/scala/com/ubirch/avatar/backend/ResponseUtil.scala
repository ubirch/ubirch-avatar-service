package com.ubirch.avatar.backend


import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import com.ubirch.avatar.model.{JsonErrorResponse, JsonResponse}

/**
  * Created by derMicha on 28/10/16.
  * This util sh
  */
trait ResponseUtil {

  def response(message: String): HttpResponse = {
    val response = JsonResponse(message = message)
    HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, response.toJsonString))
  }

  def requestErrorResponse(errorType: String, errorMessage: String): HttpResponse = {
    val response = JsonErrorResponse(errorType = errorType, errorMessage = errorMessage)
    HttpResponse(status = BadRequest, entity = HttpEntity(ContentTypes.`application/json`, response.toJsonString))
  }

  def serverErrorResponse(errorType: String, errorMessage: String): HttpResponse = {
    val response = JsonErrorResponse(errorType = errorType, errorMessage = errorMessage)
    HttpResponse(status = InternalServerError, entity = HttpEntity(ContentTypes.`application/json`, response.toJsonString))
  }
}
