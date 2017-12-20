package com.github.gvolpe.smartbackpacker.scraper

import cats.effect.Async
import com.github.gvolpe.smartbackpacker.scraper.parser.{HealthInfoParser, VisaRequirementsParser, VisaRestrictionsIndexParser}
import com.github.gvolpe.smartbackpacker.scraper.sql.{CountryInsertData, HealthInfoInsertData, VisaCategoryInsertData, VisaRequirementsInsertData, VisaRestrictionsIndexInsertData}
import doobie.util.transactor.Transactor

class ScraperModule[F[_] : Async] {

  val devDbUrl: String  = sys.env.getOrElse("JDBC_DATABASE_URL", "")
  val dbUrl: String     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")

  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  private val xa = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[F](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[F](dbDriver, dbUrl, dbUser, dbPass)
  }

  private val visaRequirementsParser  = new VisaRequirementsParser[F]()
  private val healthInfoParser        = new HealthInfoParser[F]

  val visaRequirementsInsertData      = new VisaRequirementsInsertData[F](xa, visaRequirementsParser)

  val visaRestrictionsIndexParser     = new VisaRestrictionsIndexParser[F]
  val visaRestrictionsInsertData      = new VisaRestrictionsIndexInsertData[F](xa)

  val countryInsertData               = new CountryInsertData[F](xa)
  val visaCategoryInsertData          = new VisaCategoryInsertData[F](xa)

  val healthInfoInsertData            = new HealthInfoInsertData[F](xa, healthInfoParser)

}
