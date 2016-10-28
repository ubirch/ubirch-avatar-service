package com.ubirch.avatar.model

import com.ubirch.util.json.Json4sUtil

/**
  * author: cvandrei
  * since: 2016-09-20
  */
case class JsonResponse(version: String = "1.0",
                        status: String = "OK",
                        message: String
                       ) {

  def toJsonString: String = {
    s"""
       |"version" : "$version",
       |"status" : "$status",
       |"message": "$message"
        """.stripMargin
  }

}
