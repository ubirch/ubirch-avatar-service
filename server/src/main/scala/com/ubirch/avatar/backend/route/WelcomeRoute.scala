package com.ubirch.avatar.backend.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.ubirch.avatar.config.Config
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.model.JsonResponse
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-20
  */
trait WelcomeRoute extends MyJsonProtocol {

  val route: Route = {

    get {
      complete {
        val goInfo = s"${Config.goPipelineName} / ${Config.goPipelineLabel} / ${Config.goPipelineRevision}"
        JsonResponse(message = s"Welcome to the ubirchAvatarService ( $goInfo )")
      }
    }
  }
}
