package com.github.gvolpe.smartbackpacker

import cats.effect.{Effect, IO}
import cats.syntax.semigroupk._
import com.github.gvolpe.smartbackpacker.http._
import com.github.gvolpe.smartbackpacker.http.auth.JwtTokenAuthMiddleware
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder

object Server extends HttpServer[IO]

class HttpServer[F[_] : Effect] extends StreamApp[F] {

  private val ctx = new Bindings[F]

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    Scheduler(corePoolSize = 2) flatMap { implicit scheduler =>
      for {
        authMiddleware        <- Stream.eval(JwtTokenAuthMiddleware[F](ctx.ApiToken))
        destinationInfo       = new DestinationInfoHttpEndpoint[F](ctx.countryService, ctx.httpErrorHandler).service
        airlines              = new AirlinesHttpEndpoint[F](ctx.airlineService, ctx.httpErrorHandler).service
        visaRestrictionIndex  = new VisaRestrictionIndexHttpEndpoint[F](ctx.visaRestrictionsIndexService, ctx.httpErrorHandler).service
        healthInfo            = new HealthInfoHttpEndpoint[F](ctx.healthService, ctx.httpErrorHandler).service
        httpEndpoints         = destinationInfo <+> airlines <+> visaRestrictionIndex <+> healthInfo
        exitCode              <- BlazeBuilder[F]
                                  .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
                                  .mountService(authMiddleware(httpEndpoints))
                                  .serve
      } yield exitCode
    }

}
