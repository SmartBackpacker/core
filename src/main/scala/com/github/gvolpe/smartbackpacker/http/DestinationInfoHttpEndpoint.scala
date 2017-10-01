package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import com.github.gvolpe.smartbackpacker.service.CountryService
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object DestinationInfoHttpEndpoint extends DestinationInfoHttpEndpoint

trait DestinationInfoHttpEndpoint {

  object BaseCurrencyQueryParamMatcher extends QueryParamDecoderMatcher[String]("baseCurrency")

  val service: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "traveling" / from / "to" / to :? BaseCurrencyQueryParamMatcher(baseCurrency) =>
      CountryService().destinationInformation(from, to, baseCurrency).attempt.unsafeRunSync() match {
        case Right(destinationInfo) => Ok(destinationInfo.asJson)
        case Left(error)            => BadRequest(Json.fromString(error.getMessage))
      }
  }

}
