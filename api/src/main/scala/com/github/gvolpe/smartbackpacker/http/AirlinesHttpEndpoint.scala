package com.github.gvolpe.smartbackpacker.http

import cats.MonadError
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.AirlineService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class AirlinesHttpEndpoint[F[_]](airlineService: AirlineService[F],
                                 httpErrorHandler: HttpErrorHandler[F])
                                (implicit F: MonadError[F, Throwable]) extends Http4sDsl[F] {

  object AirlineNameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "airlines" :? AirlineNameQueryParamMatcher(airline) as _ =>
      val policy = airlineService.baggagePolicy(airline.as[AirlineName])
      policy.flatMap(x => Ok(x.asJson)).recoverWith(httpErrorHandler.handler)
  }

}
