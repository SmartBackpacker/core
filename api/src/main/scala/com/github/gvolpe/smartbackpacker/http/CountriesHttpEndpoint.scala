package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.service.CountryService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class CountriesHttpEndpoint[F[_] : Monad](countryService: CountryService[F],
                                          httpErrorHandler: HttpErrorHandler[F]) extends Http4sDsl[F] {

  object BaseQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("query")

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "countries" :? BaseQueryParamMatcher(query) as _ =>
      val schengen = query.fold(false)(_.equalsIgnoreCase("schengen"))
      countryService.findAll(schengen).flatMap(x => Ok(x.asJson))
  }

}
