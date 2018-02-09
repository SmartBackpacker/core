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

package com.smartbackpackerapp.airlines.sql

import cats.effect.IO
import com.smartbackpackerapp.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import com.smartbackpackerapp.common.StreamAssertion
import com.smartbackpackerapp.common.sql.RepositorySpec
import com.smartbackpackerapp.model.{BaggageAllowance, BaggagePolicy}

class AirlinesInsertDataSpec extends RepositorySpec {

  override def testDbName: String = getClass.getSimpleName

  private val parser: AirlinesFileParser[IO] = AirlinesFileParser[IO](
    AirlineFile(getClass.getResource("/airlines-file-sample").getPath),
    AllowanceFile(getClass.getResource("/allowance-file-sample").getPath)
  )

  test("Insert airlines data from files") {
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