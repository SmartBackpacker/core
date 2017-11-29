package com.github.gvolpe.smartbackpacker

import java.nio.file.Paths

import cats.effect.{Effect, IO}
import cats.syntax.semigroupk._
import com.github.gvolpe.smartbackpacker.http._
import com.github.gvolpe.smartbackpacker.http.auth.JwtTokenAuthMiddleware
import com.github.gvolpe.smartbackpacker.http.auth.config.AuthConfig
import fs2.{Scheduler, Stream}
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.HSTS
import org.http4s.util.{ExitCode, StreamApp}

object Server extends HttpServer[IO]

class HttpServer[F[_] : Effect] extends StreamApp[F] {

  private val ctx = new Bindings[F]

  private val jwtTokenAuthMiddleware = new JwtTokenAuthMiddleware[F](AuthConfig()).middleware

  private val destinationInfoHttpEndpoint =
    new DestinationInfoHttpEndpoint[F](ctx.countryService).service

  private val airlinesHttpEndpoint =
    new AirlinesHttpEndpoint[F](ctx.airlineService).service

  private val visaRestrictionIndexHttpEndpoint =
    new VisaRestrictionIndexHttpEndpoint[F](ctx.visaRestrictionsIndexService).service

  private val httpEndpoints =
    destinationInfoHttpEndpoint <+> airlinesHttpEndpoint <+> visaRestrictionIndexHttpEndpoint

  private val authHttpEndpoints = jwtTokenAuthMiddleware(httpEndpoints)

  private val keypath = Paths.get("/home/gvolpe/development/ssl/sbkeystore").toAbsolutePath.toString
  private val keypass = sys.env.getOrElse("SB_SSL_PASSWORD", "")

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    Scheduler(corePoolSize = 2) flatMap { implicit scheduler =>
      BlazeBuilder[F]
        //.withSSL(StoreInfo(keypath, keypass), keyManagerPassword = keypass)
        //.bindHttp(sys.env.getOrElse("PORT", "8443").toInt, "0.0.0.0")
        .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
        .mountService(authHttpEndpoints)
        .serve
    }

}
