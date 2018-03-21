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

import cats.Parallel
import cats.effect.Effect
import cats.syntax.semigroupk._
import com.codahale.metrics.MetricRegistry
import com.smartbackpackerapp.config.SBConfiguration
import com.smartbackpackerapp.http._
import com.smartbackpackerapp.http.metrics.{HttpMetricsMiddleware, MetricsReporter}
import com.smartbackpackerapp.repository._
import com.smartbackpackerapp.repository.algebra._
import com.smartbackpackerapp.service._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.http4s.AuthedService
import org.http4s.client.Client

// It wires all the instances together
class Module[F[_]](httpClient: Client[F])(implicit F: Effect[F], P: Parallel[F, F]) {

  // Database config
  private val devDbUrl  = sys.env.getOrElse("JDBC_DATABASE_URL", "")
  private val devDbUser = sys.env.getOrElse("JDBC_DATABASE_USERNAME", "")
  private val devDbPass = sys.env.getOrElse("JDBC_DATABASE_PASSWORD", "")

  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUrl     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  private def xa: Transactor[F] = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[F](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[F](dbDriver, dbUrl, dbUser, dbPass)
  }

  def migrateDb: F[Unit] =
    F.delay {
      val flyway = new Flyway
      if (devDbUrl.nonEmpty) flyway.setDataSource(devDbUrl, devDbUser, devDbPass)
      else flyway.setDataSource(dbUrl, dbUser, dbPass)
      flyway.migrate()
    }

  // App Config
  private lazy val sbConfig: SBConfiguration[F] = new SBConfiguration[F]

  // Services and Repositories
  private lazy val visaRestrictionsIndexRepo: VisaRestrictionsIndexRepository[F] =
    new PostgresVisaRestrictionsIndexRepository[F](xa)

  private lazy val visaRestrictionsIndexService: VisaRestrictionIndexService[F] =
    new VisaRestrictionIndexService[F](visaRestrictionsIndexRepo)

  private lazy val airlineRepo: AirlineRepository[F] =
    new PostgresAirlineRepository[F](xa)

  private lazy val airlineService: AirlineService[F] =
    new AirlineService[F](airlineRepo)

  private lazy val visaRequirementsRepo: VisaRequirementsRepository[F] =
    new PostgresVisaRequirementsRepository[F](xa)

  private lazy val exchangeRateService: ExchangeRateService[F] =
    new ExchangeRateService[F](httpClient, sbConfig)

  private lazy val destinationInfoService: DestinationInfoService[F] =
    new DestinationInfoService[F](sbConfig, visaRequirementsRepo, exchangeRateService)

  private lazy val healthRepo: HealthRepository[F] =
    new PostgresHealthRepository[F](xa)

  private lazy val healthService: HealthService[F] =
    new HealthService[F](healthRepo)

  private lazy val countryRepository: CountryRepository[F] =
    new PostgresCountryRepository[F](xa)

  private lazy val countryService: CountryService[F] =
    new CountryService[F](countryRepository)

  // Http stuff
  private implicit val httpErrorHandler: HttpErrorHandler[F] = new HttpErrorHandler[F]

  // Http Endpoints
  private lazy val destinationInfoHttpEndpoint: AuthedService[String, F] =
    new DestinationInfoHttpEndpoint[F](destinationInfoService).service

  private lazy val airlinesHttpEndpoint: AuthedService[String, F] =
    new AirlinesHttpEndpoint[F](airlineService).service

  private lazy val visaRestrictionIndexHttpEndpoint: AuthedService[String, F] =
    new VisaRestrictionIndexHttpEndpoint[F](visaRestrictionsIndexService).service

  private lazy val healthInfoHttpEndpoint: AuthedService[String, F] =
    new HealthInfoHttpEndpoint[F](healthService).service

  private lazy val countriesHttpEndpoint: AuthedService[String, F] =
    new CountriesHttpEndpoint[F](countryService).service

  private lazy val httpEndpoints: AuthedService[String, F] =
    (destinationInfoHttpEndpoint <+> airlinesHttpEndpoint
      <+> visaRestrictionIndexHttpEndpoint <+> healthInfoHttpEndpoint
      <+> countriesHttpEndpoint)

  // Http Metrics Middleware
  private lazy val registry = new MetricRegistry()
  private lazy val metricsReporter  = new MetricsReporter[F](registry)

  lazy val startMetricsReporter: F[Unit] = {
    if (devDbUrl.nonEmpty) F.unit
    else metricsReporter.start
  }

  lazy val httpEndpointsWithMetrics: AuthedService[String, F] = {
    if (devDbUrl.nonEmpty) httpEndpoints
    else HttpMetricsMiddleware[F](registry, httpEndpoints)
  }

}
