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
import com.github.gvolpe.smartbackpacker.common.sql.RepositorySpec
import com.github.gvolpe.smartbackpacker.model.CountryCode

class HealthRepositorySpec extends RepositorySpec {

  override def testDbName: String = getClass.getSimpleName

  private lazy val repo = new PostgresHealthRepository[IO](transactor)

  test("NOT find the health information") {
    IOAssertion {
      for {
        idx <- repo.findHealthInfo(new CountryCode("AR"))
      } yield {
        assert(idx.isEmpty)
      }
    }
  }

  test("find country id query") {
    check(HealthStatement.findCountryId(new CountryCode("AR")))
  }

  test("find mandatory vaccinations query") {
    check(HealthStatement.mandatory(1))
  }

  test("find recommended vaccinations query") {
    check(HealthStatement.recommendations(1))
  }

  test("find optional vaccinations query") {
    check(HealthStatement.optional(1))
  }

  test("find health notices query") {
    check(HealthStatement.healthNotices(1))
  }

  test("find health alerts query") {
    check(HealthStatement.healthAlert(1))
  }

}