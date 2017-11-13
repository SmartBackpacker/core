package com.github.gvolpe.smartbackpacker

import cats.effect.{Effect, IO}
import com.github.gvolpe.smartbackpacker.http._
import com.github.gvolpe.smartbackpacker.service.{AirlineService, CountryService, VisaRestrictionIndexService}
import fs2.Stream
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.{ExitCode, StreamApp}

object Server extends HttpServer[IO]

class HttpServer[F[_] : Effect] extends StreamApp[F] {

  private val destinationInfoService: HttpService[F] =
    new DestinationInfoHttpEndpoint[F](CountryService[F]).service

  private val airlinesService: HttpService[F] =
    new AirlinesHttpEndpoint[F](AirlineService[F]).service

  private val visaRestrictionsService: HttpService[F] =
    new VisaRestrictionIndexHttpEndpoint[F](VisaRestrictionIndexService[F]).service

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    BlazeBuilder[F]
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
      .mountService(destinationInfoService)
      .mountService(airlinesService)
      .mountService(visaRestrictionsService)
      .serve

}
