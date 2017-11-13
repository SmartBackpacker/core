package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class VisaRestrictionIndexHttpEndpoint[F[_] : Effect]
  (visaRestrictionIndexService: VisaRestrictionIndexService[F]) extends Http4sDsl[F] {

  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "visa-restriction-index" / countryCode =>
      val ioIndex = visaRestrictionIndexService.findIndex(countryCode.as[CountryCode])
      ioIndex.>>= {
        case Some(index)  => Ok(index.asJson)
        case None         => NotFound(countryCode)
      }.recoverWith {
        case e: Exception => BadRequest(Json.fromString(e.getMessage))
      }
  }

}
