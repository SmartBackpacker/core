package com.github.gvolpe.smartbackpacker

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.http._
import fs2.Stream
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

object Server extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, Nothing] =
    BlazeBuilder[IO]
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
      .mountService(DestinationInfoHttpEndpoint.service)
      .mountService(AirlineReviewHttpEndpoint.service)
      .serve

}
