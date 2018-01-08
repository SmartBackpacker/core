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
import com.github.gvolpe.smartbackpacker.common.sql.RepositorySpec
import com.github.gvolpe.smartbackpacker.model.{Count, CountryCode, Ranking, Sharing, VisaRestrictionsIndex}

class VisaRestrictionsIndexInsertDataSpec extends RepositorySpec {

  override def testDbName: String = getClass.getSimpleName

  private val countries = List(
    (new CountryCode("AR"), VisaRestrictionsIndex(new Ranking(1), new Count(176), new Sharing(1))),
    (new CountryCode("DE"), VisaRestrictionsIndex(new Ranking(2), new Count(175), new Sharing(1))),
    (new CountryCode("JP"), VisaRestrictionsIndex(new Ranking(3), new Count(173), new Sharing(2)))
  )

  test("insert visa restrictions index data") {
    IOAssertion {
      new VisaRestrictionsIndexInsertData[IO](transactor).run(countries)
    }
  }

  test("insert visa restrictions index statement") {
    check(VisaRestrictionsIndexInsertStatement.insertVisaIndex)
  }

}