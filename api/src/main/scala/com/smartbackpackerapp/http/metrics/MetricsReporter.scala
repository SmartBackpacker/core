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

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import cats.effect.Sync
import cats.syntax.functor._
import com.codahale.metrics._
import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}

class MetricsReporter[F[_]](registry: MetricRegistry)(implicit F: Sync[F]) {

  private val graphiteHost = sys.env.getOrElse("GRAPHITE_HOST", "localhost")
  private val graphitePort = sys.env.getOrElse("GRAPHITE_PORT", "2003").toInt

  private lazy val graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort))

  private lazy val reporter = F.delay {
    GraphiteReporter
      .forRegistry(registry)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build(graphite)
  }

  val start: F[Unit] =
    reporter.map(_.start(15, TimeUnit.SECONDS))

}
