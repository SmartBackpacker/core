package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.HealthService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class HealthInfoHttpEndpoint[F[_] : Monad](healthService: HealthService[F]) extends Http4sDsl[F] {

  // TODO: Add validation for the `countryCode` parameter. Eg. only 2 characters [A-Z] and uppercase it.
  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "health" / countryCode as _ =>
      healthService.findHealthInfo(countryCode.as[CountryCode]) flatMap {
        case Some(health) => Ok(health.asJson)
        case None         => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Health info not found for $countryCode").asJson)
      }
  }

}
