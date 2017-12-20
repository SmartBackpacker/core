package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class VisaRestrictionIndexHttpEndpoint[F[_] : Monad]
   (visaRestrictionIndexService: VisaRestrictionIndexService[F],
    httpErrorHandler: HttpErrorHandler[F]) extends Http4sDsl[F] {

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "ranking" / countryCode as _ =>
      for {
        index     <- visaRestrictionIndexService.findIndex(countryCode.as[CountryCode])
        response  <- index.fold(httpErrorHandler.handle, x => Ok(x.asJson))
      } yield response
  }

}
