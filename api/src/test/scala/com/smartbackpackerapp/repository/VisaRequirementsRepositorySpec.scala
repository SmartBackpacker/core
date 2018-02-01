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

package com.smartbackpackerapp.repository

import cats.effect.IO
import com.smartbackpackerapp.common.IOAssertion
import com.smartbackpackerapp.common.sql.RepositorySpec
import com.smartbackpackerapp.model.CountryCode

class VisaRequirementsRepositorySpec extends RepositorySpec {

  override def testDbName: String = getClass.getSimpleName

  private lazy val repo = new PostgresVisaRequirementsRepository[IO](transactor)

  test("NOT find visa requirements") {
    IOAssertion {
      for {
        vr <- repo.findVisaRequirements(CountryCode("AR"), CountryCode("PL"))
      } yield {
        assert(vr.isEmpty)
      }
    }
  }

  test("find country 'from' query") {
    check(VisaRequirementsStatement.from(CountryCode("AR")))
  }

  test("find country 'to' query") {
    check(VisaRequirementsStatement.to(CountryCode("AR")))
  }

  test("find visa requirements query") {
    check(VisaRequirementsStatement.visaRequirements(1, 2))
  }

}