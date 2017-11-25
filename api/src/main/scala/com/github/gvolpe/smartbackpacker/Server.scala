package com.github.gvolpe.smartbackpacker

import cats.effect.{Effect, IO}
import com.github.gvolpe.smartbackpacker.http._
import fs2.Stream
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.{ExitCode, StreamApp}

object Server extends HttpServer[IO]

class HttpServer[F[_] : Effect] extends StreamApp[F] {

  private val ctx = new Bindings[F]

  private val destinationInfoHttpEndpoint =
    new DestinationInfoHttpEndpoint[F](ctx.countryService).service

  private val airlinesHttpEndpoint =
    new AirlinesHttpEndpoint[F](ctx.airlineService).service

  private val visaRestrictionIndexHttpEndpoint =
    new VisaRestrictionIndexHttpEndpoint[F](ctx.visaRestrictionsIndexService).service

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    BlazeBuilder[F]
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
      .mountService(destinationInfoHttpEndpoint)
      .mountService(airlinesHttpEndpoint)
      .mountService(visaRestrictionIndexHttpEndpoint)
      .serve

}
