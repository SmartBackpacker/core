package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.CountryService
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._

object DestinationInfoHttpEndpoint extends DestinationInfoHttpEndpoint

trait DestinationInfoHttpEndpoint extends Http4sClientDsl[IO] {

  object BaseCurrencyQueryParamMatcher extends QueryParamDecoderMatcher[String]("baseCurrency")

  val service: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "traveling" / from / "to" / to :? BaseCurrencyQueryParamMatcher(baseCurrency) =>
      CountryService[IO].destinationInformation(from.as[CountryCode], to.as[CountryCode], baseCurrency.as[Currency]).attempt flatMap {
        case Right(destinationInfo) => Ok(destinationInfo.asJson)
        case Left(error)            => BadRequest(Json.fromString(error.getMessage))
      }
  }

}
