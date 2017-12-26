package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.DestinationInfoService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class DestinationInfoHttpEndpoint[F[_] : Monad](destinationInfoService: DestinationInfoService[F],
                                                httpErrorHandler: HttpErrorHandler[F]) extends Http4sDsl[F] {

  object BaseCurrencyQueryParamMatcher extends QueryParamDecoderMatcher[String]("baseCurrency")

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "traveling" / from / "to" / to :? BaseCurrencyQueryParamMatcher(baseCurrency) as _ =>
      for {
        info      <- destinationInfoService.find(from.as[CountryCode], to.as[CountryCode], baseCurrency.as[Currency])
        response  <- info.fold(httpErrorHandler.handle, x => Ok(x.asJson))
      } yield response
  }

}
