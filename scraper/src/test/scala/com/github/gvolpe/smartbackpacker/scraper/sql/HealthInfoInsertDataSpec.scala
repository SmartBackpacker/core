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

package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.common.instances.log._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.scraper.config.ScraperConfiguration
import com.github.gvolpe.smartbackpacker.scraper.parser.AbstractHealthInfoParser
import doobie.free.connection.ConnectionIO
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.FunSuite

import scala.io.Source

class HealthInfoInsertDataSpec extends FunSuite with HealthInfoInsertDataFixture {

  private val scraperConfig = new ScraperConfiguration[IO]

  private val parser = new AbstractHealthInfoParser[IO] {

    override def htmlDocument(from: CountryCode): IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"healthInfo-${from.value}.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }

  }

  test("create health tables and insert data") {
    IOAssertion {
      for {
        xa <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:health_sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
        _  <- createHealthTables(xa)
        _  <- new CountryInsertData[IO](scraperConfig, xa).run
        _  <- new HealthInfoInsertData[IO](xa, parser).run("BI".as[CountryCode])
      } yield ()
    }
  }

}

trait HealthInfoInsertDataFixture {

  val countries = List(
    (new CountryCode("AR"), VisaRestrictionsIndex(new Ranking(1), new Count(176), new Sharing(1))),
    (new CountryCode("DE"), VisaRestrictionsIndex(new Ranking(2), new Count(175), new Sharing(1))),
    (new CountryCode("JP"), VisaRestrictionsIndex(new Ranking(3), new Count(173), new Sharing(2)))
  )

  private val createCountriesTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE countries (
           id SERIAL PRIMARY KEY,
           code VARCHAR (2) NOT NULL UNIQUE,
           name VARCHAR (100) NOT NULL UNIQUE,
           currency VARCHAR (3) NOT NULL,
           schengen BOOLEAN
         )
       """.update.run
  }

  private val createVaccineTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE vaccine (
           id SERIAL PRIMARY KEY,
           disease VARCHAR (200) NOT NULL,
           description VARCHAR (3000) NOT NULL,
           categories VARCHAR (1000)
         )
       """.update.run
  }

  private val createVaccineMandatoryTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE vaccine_mandatory (
           country_id INT REFERENCES countries (id),
           vaccine_id INT REFERENCES vaccine (id)
         )
       """.update.run
  }

  private val createVaccineRecommendationsTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE vaccine_recommendations (
           country_id INT REFERENCES countries (id),
           vaccine_id INT REFERENCES vaccine (id)
         )
       """.update.run
  }

  private val createVaccineOptionalTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE vaccine_optional (
           country_id INT REFERENCES countries (id),
           vaccine_id INT REFERENCES vaccine (id)
         )
       """.update.run
  }

  private val createHealthAlertTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE health_alert (
           id SERIAL PRIMARY KEY,
           title VARCHAR (500) NOT NULL,
           weblink VARCHAR (1000),
           description VARCHAR (3000)
         )
       """.update.run
  }

  private val createHealthNoticeTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE health_notice (
           country_id INT REFERENCES countries (id),
           alert_id INT REFERENCES health_alert (id),
           PRIMARY KEY (country_id, alert_id)
         )
       """.update.run
  }

  private val createHealthAlertLevelTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABlE health_alert_level (
           country_id INT PRIMARY KEY,
           alert_level VARCHAR (500) NOT NULL
         )
       """.update.run
  }

  private val createHealthAlertLevelFK: ConnectionIO[Int] =
    sql"ALTER TABLE health_alert_level ADD CONSTRAINT fk_country FOREIGN KEY (country_id) REFERENCES countries (id)"
      .update.run

  def createHealthTables(xa: Transactor[IO]): IO[Unit] =
    for {
      _ <- createCountriesTableStatement.transact(xa)
      _ <- createVaccineTableStatement.transact(xa)
      _ <- createVaccineMandatoryTableStatement.transact(xa)
      _ <- createVaccineRecommendationsTableStatement.transact(xa)
      _ <- createVaccineOptionalTableStatement.transact(xa)
      _ <- createHealthAlertTableStatement.transact(xa)
      _ <- createHealthNoticeTableStatement.transact(xa)
      _ <- createHealthAlertLevelTableStatement.transact(xa)
      _ <- createHealthAlertLevelFK.transact(xa)
    } yield ()

}