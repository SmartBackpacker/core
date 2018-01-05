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
import com.github.gvolpe.smartbackpacker.model.{Count, CountryCode, Ranking, Sharing, VisaRestrictionsIndex}
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.scalatest.FunSuite

class VisaRestrictionsIndexInsertDataSpec extends FunSuite with VisaRestrictionsIndexFixture {

  test("create table visa_restrictions_index and insert data") {
    IOAssertion {
      for {
        xa <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
        _  <- createVisaIndexTable(xa)
        _  <- new VisaRestrictionsIndexInsertData[IO](xa).run(countries)
      } yield ()
    }
  }

}

trait VisaRestrictionsIndexFixture {

  val countries = List(
    (new CountryCode("AR"), VisaRestrictionsIndex(new Ranking(1), new Count(176), new Sharing(1))),
    (new CountryCode("DE"), VisaRestrictionsIndex(new Ranking(2), new Count(175), new Sharing(1))),
    (new CountryCode("JP"), VisaRestrictionsIndex(new Ranking(3), new Count(173), new Sharing(2)))
  )

  private def createVisaIndexTableStatement: Update0 =
    sql"""
         CREATE TABLE visa_restrictions_index (
           country_code VARCHAR (2) PRIMARY KEY,
           rank SMALLINT NOT NULL,
           acc SMALLINT NOT NULL,
           sharing SMALLINT DEFAULT 0
         )
       """.update

  def createVisaIndexTable(xa: Transactor[IO]): IO[Int] =
    createVisaIndexTableStatement.run.transact(xa)
}