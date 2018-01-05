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
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.scalatest.FunSuite

class VisaCategoryInsertDataSpec extends FunSuite with VisaCategoryFixture {

  test("create table visa_category and insert data") {
    IOAssertion {
      for {
        xa <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
        _  <- createVisaCategoryTable(xa)
        _  <- new VisaCategoryInsertData[IO](xa).run
      } yield ()
    }
  }

}

trait VisaCategoryFixture {

  private def createVisaCategoryTableStatement: Update0 =
    sql"""
         CREATE TABLE visa_category (
           id SERIAL PRIMARY KEY,
           name VARCHAR (200)
         )
       """.update

  def createVisaCategoryTable(xa: Transactor[IO]): IO[Int] =
    createVisaCategoryTableStatement.run.transact(xa)

}
