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
import com.github.gvolpe.smartbackpacker.common.sql.TestDBManager
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class CountryRepositorySpec extends FunSuite with IOChecker with BeforeAndAfterAll {

  override val transactor: Transactor[IO] = TestDBManager.xa.unsafeRunSync()

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDBManager.createTables.unsafeRunSync()
  }

  private lazy val repo = new PostgresCountryRepository[IO](transactor)

  test("NOT find all the countries and schengen countries") {
    IOAssertion {
      for {
        rs1 <- repo.findAll
        rs2 <- repo.findSchengen
      } yield {
        assert(rs1.isEmpty)
        assert(rs2.isEmpty)
      }
    }
  }

  test("find countries query") {
    check(CountryStatement.findCountries)
  }

  test("find schengen countries query") {
    check(CountryStatement.findSchengen)
  }

}