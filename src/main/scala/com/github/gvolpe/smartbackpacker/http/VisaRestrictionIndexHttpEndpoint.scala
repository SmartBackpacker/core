package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import cats.syntax.applicativeError._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._

object VisaRestrictionIndexHttpEndpoint extends VisaRestrictionIndexHttpEndpoint

trait VisaRestrictionIndexHttpEndpoint extends Http4sClientDsl[IO] {

  val service: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "visa-restriction-index" / countryCode =>
      val ioIndex = VisaRestrictionIndexService[IO].findIndex(countryCode.as[CountryCode])
      ioIndex.flatMap {
        case Some(index)  => Ok(index.asJson)
        case None         => NotFound(countryCode)
      }.recoverWith {
        case e: Exception => BadRequest(Json.fromString(e.getMessage))
      }
  }

}
