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

package com.github.gvolpe.smartbackpacker.repository

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model.CountryCode
import doobie.free.connection.ConnectionIO
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class HealthRepositorySpec extends HealthSQLSetup with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override val h2Transactor: IO[H2Transactor[IO]] =
    H2Transactor[IO]("jdbc:h2:mem:health_sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")

  override def beforeAll(): Unit = {
    super.beforeAll()
    setup.unsafeRunSync()
  }

  it should "NOT find the health information" in IOAssertion {
    for {
      xa  <- h2Transactor
      idx <- new PostgresHealthRepository[IO](xa).findHealthInfo(new CountryCode("AR"))
    } yield {
      idx should be (None)
    }
  }

}

trait HealthSQLSetup {

  def h2Transactor: IO[H2Transactor[IO]]

  lazy val setup: IO[Unit] =
    for {
      xa  <- h2Transactor
      _   <- createHealthTables(xa)
    } yield ()

  private def createCountriesTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE countries (
           id SERIAL PRIMARY KEY,
           code VARCHAR (2) NOT NULL UNIQUE,
           name VARCHAR (100) NOT NULL UNIQUE
         )
       """.update.run
  }

  private def createVaccineTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE vaccine (
           id SERIAL PRIMARY KEY,
           disease VARCHAR (200) NOT NULL,
           description VARCHAR (3000) NOT NULL,
           categories VARCHAR (1000)
         )
       """.update.run
  }

  private def createVaccineMandatoryTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE vaccine_mandatory (
           country_id INT REFERENCES countries (id),
           vaccine_id INT REFERENCES vaccine (id)
         )
       """.update.run
  }

  private def createVaccineRecommendationsTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE vaccine_recommendations (
           country_id INT REFERENCES countries (id),
           vaccine_id INT REFERENCES vaccine (id)
         )
       """.update.run
  }

  private def createVaccineOptionalTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE vaccine_optional (
           country_id INT REFERENCES countries (id),
           vaccine_id INT REFERENCES vaccine (id)
         )
       """.update.run
  }

  private def createHealthAlertTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE health_alert (
           id SERIAL PRIMARY KEY,
           title VARCHAR (500) NOT NULL,
           weblink VARCHAR (1000),
           description VARCHAR (3000)
         )
       """.update.run
  }

  private def createHealthNoticeTableStatement: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE health_notice (
           country_id INT REFERENCES countries (id),
           alert_id INT REFERENCES health_alert (id),
           PRIMARY KEY (country_id, alert_id)
         )
       """.update.run
  }

  private def createHealthAlertLevelTableStatement: ConnectionIO[Int] = {
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

  private def createHealthTables(xa: Transactor[IO]): IO[Unit] =
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
