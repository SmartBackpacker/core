package com.github.gvolpe.smartbackpacker

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.http.DestinationInfoHttpEndpoint
import fs2.Stream
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import org.http4s._
import org.http4s.server.Server
//import org.http4s.dsl.io._

import org.http4s.server.blaze._
import org.http4s.server.syntax._
import cats.implicits._

object Server extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, Nothing] =
    //val services = tweetService |+| helloWorldService
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(DestinationInfoHttpEndpoint.service)
      .serve

}
