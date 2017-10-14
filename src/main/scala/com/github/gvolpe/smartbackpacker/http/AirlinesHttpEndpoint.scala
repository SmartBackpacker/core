package com.github.gvolpe.smartbackpacker.http

import cats.effect._
import com.github.gvolpe.smartbackpacker.service.AirlineService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._

object AirlinesHttpEndpoint extends AirlinesHttpEndpoint

trait AirlinesHttpEndpoint extends Http4sClientDsl[IO] {

  val service: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "airlines" =>
      Ok(AirlineService[IO].baggagePolicy.unsafeRunSync().asJson)
  }

}
