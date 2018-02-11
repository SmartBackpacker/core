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

package com.smartbackpackerapp.http.metrics

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.codahale.metrics._
import com.smartbackpackerapp.common.Log
import com.smartbackpackerapp.http.ApiVersion
import org.http4s.AuthedService
import org.http4s.Uri.Path

object HttpMetricsMiddleware {

  def apply[F[_]](registry: MetricRegistry,
                  service: AuthedService[String, F])
                 (implicit F: Sync[F], L: Log[F]): AuthedService[String, F] = {

    Kleisli { req =>
      OptionT.liftF(F.delay(System.nanoTime())).flatMap { start =>
        service(req).semiflatMap { response =>
          HttpMetrics.parse(req.req.uri.path).fold(F.delay(response)) { path =>
            for {
              _    <- F.delay(registry.meter(s"requests-$path").mark())
              _    <- if (response.status.isSuccess) F.delay(registry.meter(s"success-$path").mark())
                      else F.delay(registry.meter(s"failure-${response.status.code}-$path").mark())
              time <- F.delay((System.nanoTime() - start) / 1000000)
              _    <- F.delay(registry.histogram(s"response-time-$path").update(time))
              _    <- L.info(s"HTTP Response Time: $time ms")
            } yield response
          }
        }
      }
    }
  }

}

object HttpMetrics {

  def parse(path: Path): Option[String] = {
    if (path.contains("/traveling")) Some(s"$ApiVersion-traveling")
    else if (path.contains("/airlines")) Some(s"$ApiVersion-airlines")
    else if (path.contains("/ranking")) Some(s"$ApiVersion-ranking")
    else if (path.contains("/health")) Some(s"$ApiVersion-health")
    else if (path.contains("/countries")) Some(s"$ApiVersion-countries")
    else None
  }

}
