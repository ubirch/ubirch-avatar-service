package com.ubirch.avatar.model

/**
  * Created by derMicha on 28/10/16.
  */
case class JsonErrorResponse(version: String = "1.0",
                             status: String = "NOK",
                             errorType: String,
                             errorMessage: String
                                  ) {

  def toJsonString: String = {
    s"""{
        |  "version" : "$version",
        |  "status" : "$status",
        |  "errorType" : "$errorType",
        |  "errorMessage": "$errorMessage"
        |}""".stripMargin
  }
}
