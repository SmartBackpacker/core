package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.AirlineService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class AirlinesHttpEndpoint[F[_] : Monad](airlineService: AirlineService[F],
                                         httpErrorHandler: HttpErrorHandler[F]) extends Http4sDsl[F] {

  object AirlineNameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "airlines" :? AirlineNameQueryParamMatcher(airline) as _ =>
      for {
        policy    <- airlineService.baggagePolicy(airline.as[AirlineName])
        response  <- policy.fold(httpErrorHandler.handle, x => Ok(x.asJson))
      } yield response
  }

}
