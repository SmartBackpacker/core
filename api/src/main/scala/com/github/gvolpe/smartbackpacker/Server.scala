package com.github.gvolpe.smartbackpacker

import cats.effect.{Effect, IO}
import com.github.gvolpe.smartbackpacker.http._
import fs2.Stream
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.{ExitCode, StreamApp}

object Server extends HttpServer[IO]

class HttpServer[F[_] : Effect] extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    BlazeBuilder[F]
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
      .mountService(DestinationInfoHttpEndpoint[F].service)
      .mountService(AirlinesHttpEndpoint[F].service)
      .mountService(VisaRestrictionIndexHttpEndpoint[F].service)
      .serve

}
