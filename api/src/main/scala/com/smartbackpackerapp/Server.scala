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
import org.http4s.server.blaze.BlazeBuilder

object Server extends HttpServer[IO]

class HttpServer[F[_] : Effect] extends StreamApp[F] {

  private val ctx = new Module[F]

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      _              <- Scheduler(corePoolSize = 2)
      _              <- Stream.eval(ctx.migrateDb)
      apiToken       <- Stream.eval(ctx.ApiToken)
      authMiddleware <- Stream.eval(JwtTokenAuthMiddleware[F](apiToken))
      exitCode       <- BlazeBuilder[F]
                          .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
                          .mountService(authMiddleware(ctx.httpEndpoints))
                          .serve
    } yield exitCode

}
