package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import cats.syntax.applicativeError._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._

class VisaRestrictionIndexHttpEndpoint[F[_] : Effect]
  (visaRestrictionIndexService: VisaRestrictionIndexService[F]) extends BaseHttpEndpoint[F] {

  import effectDsl._

  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "visa-restriction-index" / countryCode =>
      val ioIndex = visaRestrictionIndexService.findIndex(countryCode.as[CountryCode])
      Effect[F].>>=(ioIndex) {
        case Some(index)  => Ok(index.asJson)
        case None         => NotFound(countryCode)
      }.recoverWith {
        case e: Exception => BadRequest(Json.fromString(e.getMessage))
      }
  }

}
