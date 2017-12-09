package com.github.gvolpe.smartbackpacker

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.persistence.{AirlineDao, PostgresAirlineDao, PostgresVisaRequirementsDao, PostgresVisaRestrictionsIndexDao, VisaRequirementsDao, VisaRestrictionsIndexDao}
import com.github.gvolpe.smartbackpacker.service.{AirlineService, CountryService, ExchangeRateService, VisaRestrictionIndexService}
import doobie.util.transactor.Transactor

// It wires all the instances together
class Bindings[F[_] : Effect] {

  private val devDbUrl  = sys.env.getOrElse("JDBC_DATABASE_URL", "")

  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUrl     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  def xa: Transactor[F] = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[F](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[F](dbDriver, dbUrl, dbUser, dbPass)
  }

  lazy val ApiToken = sys.env.get("SB_API_TOKEN")

  lazy val visaRestrictionsIndexDao: VisaRestrictionsIndexDao[F] =
    new PostgresVisaRestrictionsIndexDao[F](xa)

  lazy val visaRestrictionsIndexService: VisaRestrictionIndexService[F] =
    new VisaRestrictionIndexService[F](visaRestrictionsIndexDao)

  lazy val airlineDao: AirlineDao[F] =
    new PostgresAirlineDao[F](xa)

  lazy val airlineService: AirlineService[F] =
    new AirlineService[F](airlineDao)

  lazy val visaRequirementsDao: VisaRequirementsDao[F] =
    new PostgresVisaRequirementsDao[F](xa)

  lazy val countryService: CountryService[F] =
    new CountryService[F](visaRequirementsDao, ExchangeRateService[F])

}
