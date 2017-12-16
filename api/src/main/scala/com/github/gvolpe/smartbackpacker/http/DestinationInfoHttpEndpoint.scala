package com.github.gvolpe.smartbackpacker.http

import cats.MonadError
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.CountryService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class DestinationInfoHttpEndpoint[F[_]](countryService: CountryService[F],
                                        httpErrorHandler: HttpErrorHandler[F])
                                       (implicit F: MonadError[F, Throwable]) extends Http4sDsl[F] {

  object BaseCurrencyQueryParamMatcher extends QueryParamDecoderMatcher[String]("baseCurrency")

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "traveling" / from / "to" / to :? BaseCurrencyQueryParamMatcher(baseCurrency) as _ =>
      val info = countryService.destinationInformation(from.as[CountryCode], to.as[CountryCode], baseCurrency.as[Currency])
      info.flatMap(x => Ok(x.asJson)).recoverWith(httpErrorHandler.handler)
  }

}
