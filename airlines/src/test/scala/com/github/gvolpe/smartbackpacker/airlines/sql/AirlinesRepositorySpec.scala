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

package com.github.gvolpe.smartbackpacker.airlines.sql

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import com.github.gvolpe.smartbackpacker.common.StreamAssertion
import com.github.gvolpe.smartbackpacker.common.sql.TestDBManager
import com.github.gvolpe.smartbackpacker.model.{BaggageAllowance, BaggagePolicy}
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class AirlinesRepositorySpec extends FunSuite with IOChecker with BeforeAndAfterAll {

  override val transactor: Transactor[IO] = TestDBManager.xa.unsafeRunSync()

  private val parser: AirlinesFileParser[IO] = AirlinesFileParser[IO](
    new AirlineFile(getClass.getResource("/airlines-file-sample").getPath),
    new AllowanceFile(getClass.getResource("/allowance-file-sample").getPath)
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDBManager.createTables.unsafeRunSync()
  }

  test("Create tables and insert data from files") {
    StreamAssertion {
      new AirlinesInsertData[IO](transactor, parser).run
    }
  }

  test("Insert airline query") {
    check(AirlineInsertStatement.insertAirline("Ryan Air"))
  }

  test("Insert airline's baggage policy") {
    check(AirlineInsertStatement.insertBaggagePolicy(1, BaggagePolicy(List.empty[BaggageAllowance], None, None)))
  }

}