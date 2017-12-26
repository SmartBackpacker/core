package com.github.gvolpe.smartbackpacker

import cats.effect.{Effect, IO}
import com.github.gvolpe.smartbackpacker.http.auth.JwtTokenAuthMiddleware
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder

object Server extends HttpServer[IO]

class HttpServer[F[_] : Effect] extends StreamApp[F] {

  private val ctx = new Module[F]

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      _              <- Scheduler(corePoolSize = 2)
      apiToken       <- Stream.eval(ctx.ApiToken)
      authMiddleware <- Stream.eval(JwtTokenAuthMiddleware[F](apiToken))
      exitCode       <- BlazeBuilder[F]
                          .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
                          .mountService(authMiddleware(ctx.httpEndpoints))
                          .serve
    } yield exitCode

}
