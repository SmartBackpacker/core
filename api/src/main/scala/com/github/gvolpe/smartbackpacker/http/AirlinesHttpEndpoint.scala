package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import cats.syntax.applicativeError._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.AirlineService
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._

class AirlinesHttpEndpoint[F[_] : Effect](airlineService: AirlineService[F]) extends BaseHttpEndpoint[F] {

  import effectDsl._

  object AirlineNameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")

  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "airlines" :? AirlineNameQueryParamMatcher(airline) =>
      val policy = airlineService.baggagePolicy(airline.as[AirlineName])
      Effect[F].>>=(policy)(x => Ok(x.asJson)).recoverWith{
        case e: Exception => BadRequest(Json.fromString(e.getMessage))
      }
  }

}
