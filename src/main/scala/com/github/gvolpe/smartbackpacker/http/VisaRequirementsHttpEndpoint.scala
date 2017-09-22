package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import com.github.gvolpe.smartbackpacker.parser.WikiPageParser

import io.circe.generic.auto._
import io.circe.syntax._

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object VisaRequirementsHttpEndpoint extends VisaRequirementsHttpEndpoint

trait VisaRequirementsHttpEndpoint {

  // TODO: Make the call asynchronous
  val service = HttpService[IO] {
    case GET -> Root / "visa-requirements" / from / "to" / to =>
      WikiPageParser.visaRequirementsFor(from, to) match {
        case Some(value)  => Ok(value.asJson)
        case None         => NotFound()
      }
  }

}
