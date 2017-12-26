package com.github.gvolpe.smartbackpacker

import cats.effect.Effect
import cats.syntax.semigroupk._ // For appending http services with <+>
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.http._
import com.github.gvolpe.smartbackpacker.repository._
import com.github.gvolpe.smartbackpacker.repository.algebra._
import com.github.gvolpe.smartbackpacker.service._
import doobie.util.transactor.Transactor
import org.http4s.AuthedService
import org.http4s.client.blaze.PooledHttp1Client

// It wires all the instances together
class Module[F[_]](implicit F: Effect[F]) {

  // Database config
  private val devDbUrl  = sys.env.getOrElse("JDBC_DATABASE_URL", "")
  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUrl     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  private def xa: Transactor[F] = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[F](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[F](dbDriver, dbUrl, dbUser, dbPass)
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
    new ExchangeRateService[F](PooledHttp1Client[F](), sbConfig)

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
  private lazy val httpErrorHandler: HttpErrorHandler[F] = new HttpErrorHandler[F]

  lazy val ApiToken: F[Option[String]] = F.delay(sys.env.get("SB_API_TOKEN"))

  // Http Endpoints
  private lazy val destinationInfoHttpEndpoint: AuthedService[String, F] =
    new DestinationInfoHttpEndpoint[F](destinationInfoService, httpErrorHandler).service

  private lazy val airlinesHttpEndpoint: AuthedService[String, F] =
    new AirlinesHttpEndpoint[F](airlineService, httpErrorHandler).service

  private lazy val visaRestrictionIndexHttpEndpoint: AuthedService[String, F] =
    new VisaRestrictionIndexHttpEndpoint[F](visaRestrictionsIndexService, httpErrorHandler).service

  private lazy val healthInfoHttpEndpoint: AuthedService[String, F] =
    new HealthInfoHttpEndpoint[F](healthService, httpErrorHandler).service

  private lazy val countriesHttpEndpoint: AuthedService[String, F] =
    new CountriesHttpEndpoint[F](countryService, httpErrorHandler).service

  lazy val httpEndpoints: AuthedService[String, F] =
    (destinationInfoHttpEndpoint <+> airlinesHttpEndpoint
      <+> visaRestrictionIndexHttpEndpoint <+> healthInfoHttpEndpoint
      <+> countriesHttpEndpoint)

}
