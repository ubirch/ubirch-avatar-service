package com.ubirch.avatar.model

import com.ubirch.util.json.MyJsonProtocol
import org.json4s.native.Serialization.write

/**
  * author: cvandrei
  * since: 2016-09-23
  */
case class ErrorResponse(apiVersion: String = "1.0",
                         status: String = "OK",
                         error: Error
                        )

case class Error(errorId: String,
                 errorMessage: String)

object ErrorFactory extends MyJsonProtocol {

  def create(errorId: String, errorMessage: String): ErrorResponse = ErrorResponse(error = Error(errorId = errorId, errorMessage = errorMessage))

  def createString(errorId: String, errorMessage: String): String = {
    val errorObject = create(errorId, errorMessage)
    write(errorObject)
  }

}
