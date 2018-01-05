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
import com.github.gvolpe.smartbackpacker.model._
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class VisaRequirementsRepositorySpec extends VisaRequirementsSQLSetup with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override val h2Transactor: IO[H2Transactor[IO]] =
    H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")

  override def beforeAll(): Unit = {
    super.beforeAll()
    setup.unsafeRunSync()
  }

  it should "NOT find visa requirements" in IOAssertion {
    for {
      xa <- h2Transactor
      vr <- new PostgresVisaRequirementsRepository[IO](xa).findVisaRequirements("AR".as[CountryCode], "PL".as[CountryCode])
    } yield {
      vr should be (None)
    }
  }

}

trait VisaRequirementsSQLSetup {

  def h2Transactor: IO[H2Transactor[IO]]

  lazy val setup: IO[Unit] =
    for {
      xa  <- h2Transactor
      _   <- createTables(xa)
    } yield ()

  private val createTableCountries: Update0 =
    sql"""
         CREATE TABLE countries (
           id SERIAL PRIMARY KEY,
           code VARCHAR (2) NOT NULL UNIQUE,
           name VARCHAR (100) NOT NULL UNIQUE,
           currency VARCHAR (3) NOT NULL,
           schengen BOOLEAN
         )
       """.update

  private val createTableVisaCategory: Update0 =
    sql"""
         CREATE TABLE visa_category (
           id SERIAL PRIMARY KEY,
           name VARCHAR (200)
         )
       """.update

  private val createTableVisaRequirements: Update0 =
    sql"""
         CREATE TABLE visa_requirements (
           from_country INT NOT NULL,
           to_country INT NOT NULL,
           visa_category INT REFERENCES visa_category (id),
           description VARCHAR (500)
         )
       """.update

  private val createVisaRequirementsFK1: Update0 =
    sql"ALTER TABLE visa_requirements ADD CONSTRAINT fk_from FOREIGN KEY (from_country) REFERENCES countries (id)"
      .update

  private val createVisaRequirementsFK2: Update0 =
    sql"ALTER TABLE visa_requirements ADD CONSTRAINT fk_to FOREIGN KEY (to_country) REFERENCES countries (id)"
      .update

  private val createVisaRequirementsPK: Update0 =
    sql"ALTER TABLE visa_requirements ADD PRIMARY KEY (from_country, to_country)"
      .update

  def createTables(xa: Transactor[IO]): IO[Unit] =
    for {
      _ <- createTableCountries.run.transact(xa)
      _ <- createTableVisaCategory.run.transact(xa)
      _ <- createTableVisaRequirements.run.transact(xa)
      _ <- createVisaRequirementsFK1.run.transact(xa)
      _ <- createVisaRequirementsFK2.run.transact(xa)
      _ <- createVisaRequirementsPK.run.transact(xa)
    } yield ()

}
