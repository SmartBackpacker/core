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

package com.smartbackpackerapp.scraper.sql

import cats.effect.IO
import com.smartbackpackerapp.common.IOAssertion
import com.smartbackpackerapp.common.sql.RepositorySpec
import com.smartbackpackerapp.scraper.config.ScraperConfiguration

class CountryInsertDataSpec extends RepositorySpec {

  override def testDbName: String = getClass.getSimpleName

  private val scraperConfig = new ScraperConfiguration[IO]
  private val repo = new CountryInsertData[IO](scraperConfig, transactor)

  test("insert and update country data") {
    IOAssertion {
      for {
        _  <- repo.run
        _  <- repo.runUpdate
      } yield ()
    }
  }

  test("insert countries statement") {
    check(CountryInsertStatement.insertCountries)
  }

  test("update countries currency statement") {
    check(CountryInsertStatement.updateCountriesCurrency)
  }

  test("update schengen countries statement") {
    check(CountryInsertStatement.updateSchengenCountries)
  }

}