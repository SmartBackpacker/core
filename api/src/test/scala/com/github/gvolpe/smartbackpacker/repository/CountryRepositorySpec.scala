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
import doobie.h2._
import doobie.implicits._
import doobie.util.update.Update0
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class CountryRepositorySpec extends CountrySQLSetup with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override val h2Transactor: IO[H2Transactor[IO]] =
    H2Transactor[IO]("jdbc:h2:mem:country_sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")

  override def beforeAll(): Unit = {
    super.beforeAll()
    setup.unsafeRunSync()
  }

  it should "NOT find all the countries and schengen countries" in IOAssertion {
    for {
      xa    <- h2Transactor
      repo  = new PostgresCountryRepository[IO](xa)
      rs1   <- repo.findAll
      rs2   <- repo.findSchengen
    } yield {
      rs1 should be (empty)
      rs2 should be (empty)
    }
  }

}

trait CountrySQLSetup {

  def h2Transactor: IO[H2Transactor[IO]]

  lazy val setup: IO[Unit] =
    for {
      xa  <- h2Transactor
      _   <- createTableCountries.run.transact(xa)
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

}