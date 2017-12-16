package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class VisaRestrictionIndexHttpEndpoint[F[_] : Monad]
   (visaRestrictionIndexService: VisaRestrictionIndexService[F]) extends Http4sDsl[F] {

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "ranking" / countryCode as _ =>
      val ioIndex = visaRestrictionIndexService.findIndex(countryCode.as[CountryCode])
      ioIndex.flatMap {
        case Some(index)  => Ok(index.asJson)
        case None         => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Country not found $countryCode").asJson)
      }
  }

}
