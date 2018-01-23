/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbackpackerapp

import cats.effect.{Effect, IO}
import com.smartbackpackerapp.http.auth.JwtTokenAuthMiddleware
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import org.http4s.client.blaze.Http1Client
import org.http4s.server.blaze.BlazeBuilder

object Server extends HttpServer[IO]

class HttpServer[F[_]](implicit F: Effect[F]) extends StreamApp[F] {

  private lazy val ApiToken: F[Option[String]] = F.delay(sys.env.get("SB_API_TOKEN"))

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    Scheduler(corePoolSize = 2).flatMap { implicit scheduler =>
      for {
        httpClient      <- Stream.eval(Http1Client[F]())
        ctx             = new Module[F](httpClient)
        _               <- Stream.eval(ctx.migrateDb)
        apiToken        <- Stream.eval(ApiToken)
        authMiddleware  <- Stream.eval(JwtTokenAuthMiddleware[F](apiToken))
        exitCode        <- BlazeBuilder[F]
                            .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
                            .mountService(authMiddleware(ctx.httpEndpoints))
                            .serve
      } yield exitCode
    }

}
