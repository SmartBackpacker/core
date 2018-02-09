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

package com.smartbackpackerapp.scraper

import cats.effect.Async
import com.smartbackpackerapp.scraper.config.ScraperConfiguration
import com.smartbackpackerapp.scraper.parser.{HealthInfoParser, VisaRequirementsParser, VisaRestrictionsIndexParser}
import com.smartbackpackerapp.scraper.sql.{CountryInsertData, HealthInsertData, VisaCategoryInsertData, VisaRequirementsInsertData, VisaRestrictionsIndexInsertData}
import doobie.util.transactor.Transactor

class ScraperModule[F[_] : Async] {

  lazy val scraperConfig     = new ScraperConfiguration[F]

  lazy val devDbUrl: String  = sys.env.getOrElse("JDBC_DATABASE_URL", "")
  lazy val dbUrl: String     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")

  private lazy val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private lazy val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private lazy val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  private val xa = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[F](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[F](dbDriver, dbUrl, dbUser, dbPass)
  }

  private val visaRequirementsParser  = new VisaRequirementsParser[F](scraperConfig)
  private val healthInfoParser        = new HealthInfoParser[F](scraperConfig)

  val visaRequirementsInsertData      = new VisaRequirementsInsertData[F](xa, visaRequirementsParser)

  val visaRestrictionsIndexParser     = new VisaRestrictionsIndexParser[F](scraperConfig)
  val visaRestrictionsInsertData      = new VisaRestrictionsIndexInsertData[F](xa)

  val countryInsertData               = new CountryInsertData[F](scraperConfig, xa)
  val visaCategoryInsertData          = new VisaCategoryInsertData[F](xa)

  val healthInfoInsertData            = new HealthInsertData[F](xa, healthInfoParser)

}
