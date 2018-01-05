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
import com.github.gvolpe.smartbackpacker.scraper.config.ScraperConfiguration
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.scalatest.FunSuite

class CountryInsertDataSpec extends FunSuite with CountryFixture {

  private val scraperConfig = new ScraperConfiguration[IO]

  test("create table country and insert data") {
    IOAssertion {
      for {
        xa <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
        _  <- createCountryTable(xa)
        cd = new CountryInsertData[IO](scraperConfig, xa)
        _  <- cd.run
        _  <- cd.runUpdate
      } yield ()
    }
  }

}

trait CountryFixture {

  private def createCountryTableStatement: Update0 =
    sql"""
         CREATE TABLE countries (
           id SERIAL PRIMARY KEY,
           code VARCHAR (2) NOT NULL UNIQUE,
           name VARCHAR (100) NOT NULL UNIQUE,
           currency VARCHAR (3) NOT NULL,
           schengen BOOLEAN
         )
       """.update

  def createCountryTable(xa: Transactor[IO]): IO[Int] =
    createCountryTableStatement.run.transact(xa)

}
