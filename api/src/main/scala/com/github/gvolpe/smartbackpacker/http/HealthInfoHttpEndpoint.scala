package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.HealthService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class HealthInfoHttpEndpoint[F[_] : Monad](healthService: HealthService[F],
                                           httpErrorHandler: HttpErrorHandler[F]) extends Http4sDsl[F] {

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "health" / countryCode as _ =>
      for {
        healthInfo  <- healthService.findHealthInfo(countryCode.as[CountryCode])
        response    <- healthInfo.fold(httpErrorHandler.handle, x => Ok(x.asJson))
      } yield response
  }

}
