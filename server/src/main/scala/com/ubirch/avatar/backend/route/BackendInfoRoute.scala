package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.{ Directives, Route }
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.server.util.ServerKeys
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

class BackendInfoRoute extends Directives with CORSDirective with ResponseUtil with StrictLogging {

  case class BackendPubKey(
    algorithm: String = "Ed25519",
    publicKeyHex: String,
    publicKeyBase64: String
  )

  val route: Route = {

    path(RouteConstants.backendinfo / RouteConstants.pubkey) {
      respondWithCORS {
        get {

          val goInfo = s"${Config.goPipelineName} / ${Config.goPipelineLabel} / ${Config.goPipelineRevision}"
          val pubKey = BackendPubKey(
            publicKeyHex = ServerKeys.pubKeyHex,
            publicKeyBase64 = ServerKeys.pubKeyB64
          )
          complete(OK -> pubKey)
        }
      }
    }
  }
}
