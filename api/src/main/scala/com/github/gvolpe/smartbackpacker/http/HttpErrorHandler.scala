package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import com.github.gvolpe.smartbackpacker.service._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.Response

class HttpErrorHandler[F[_] : Monad] extends Http4sDsl[F] {

  val handle: ValidationError => F[Response[F]] = {
    case CountriesMustBeDifferent           => BadRequest(ApiError(ApiErrorCode.SAME_COUNTRIES_SEARCH, "Countries must be different!").asJson)
    case CountryNotFound(cc)                => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Country not found ${cc.value}").asJson)
    case AirlineNotFound(a)                 => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Airline not found ${a.value}").asJson)
    case VisaRestrictionsIndexNotFound(cc)  => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Visa restrictions index not found for ${cc.value}").asJson)
    case HealthInfoNotFound(cc)             => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Health info not found for ${cc.value}").asJson)
  }

}
