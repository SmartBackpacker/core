package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.CountryService
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class DestinationInfoHttpEndpoint[F[_] : Effect](countryService: CountryService[F]) extends Http4sDsl[F] {

  object BaseCurrencyQueryParamMatcher extends QueryParamDecoderMatcher[String]("baseCurrency")

  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "traveling" / from / "to" / to :? BaseCurrencyQueryParamMatcher(baseCurrency) =>
      val info = countryService.destinationInformation(from.as[CountryCode], to.as[CountryCode], baseCurrency.as[Currency])
      info.flatMap(x => Ok(x.asJson)).recoverWith {
        case e: Exception => BadRequest(Json.fromString(e.getMessage))
      }
  }

}