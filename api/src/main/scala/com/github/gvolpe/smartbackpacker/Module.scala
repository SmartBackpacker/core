package com.github.gvolpe.smartbackpacker

import cats.effect.Effect
import cats.syntax.semigroupk._
import com.github.gvolpe.smartbackpacker.http._
import com.github.gvolpe.smartbackpacker.repository._
import com.github.gvolpe.smartbackpacker.repository.algebra._
import com.github.gvolpe.smartbackpacker.service._
import doobie.util.transactor.Transactor
import org.http4s.AuthedService

// It wires all the instances together
class Module[F[_] : Effect] {

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

  private lazy val countryService: CountryService[F] =
    new CountryService[F](visaRequirementsRepo, ExchangeRateService[F])

  private lazy val healthRepo: HealthRepository[F] =
    new PostgresHealthRepository[F](xa)

  private lazy val healthService: HealthService[F] =
    new HealthService[F](healthRepo)

  // Http stuff
  private lazy val httpErrorHandler: HttpErrorHandler[F] = new HttpErrorHandler[F]

  lazy val ApiToken: Option[String] = sys.env.get("SB_API_TOKEN")

  // Http Endpoints
  private lazy val destinationInfoHttpEndpoint: AuthedService[String, F] =
    new DestinationInfoHttpEndpoint[F](countryService, httpErrorHandler).service

  private lazy val airlinesHttpEndpoint: AuthedService[String, F] =
    new AirlinesHttpEndpoint[F](airlineService, httpErrorHandler).service

  private lazy val visaRestrictionIndexHttpEndpoint: AuthedService[String, F] =
    new VisaRestrictionIndexHttpEndpoint[F](visaRestrictionsIndexService, httpErrorHandler).service

  private lazy val healthInfoHttpEndpoint: AuthedService[String, F] =
    new HealthInfoHttpEndpoint[F](healthService, httpErrorHandler).service

  lazy val httpEndpoints: AuthedService[String, F] =
    destinationInfoHttpEndpoint <+> airlinesHttpEndpoint <+> visaRestrictionIndexHttpEndpoint <+> healthInfoHttpEndpoint

}
