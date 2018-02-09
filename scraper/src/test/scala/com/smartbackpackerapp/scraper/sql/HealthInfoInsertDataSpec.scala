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
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.scraper.config.ScraperConfiguration
import com.smartbackpackerapp.scraper.parser.AbstractHealthInfoParser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document

import scala.io.Source

class HealthInfoInsertDataSpec extends RepositorySpec {

  override def testDbName: String = getClass.getSimpleName

  private val scraperConfig = new ScraperConfiguration[IO]

  private val parser = new AbstractHealthInfoParser[IO] {

    override def htmlDocument(from: CountryCode): IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"healthInfo-${from.value}.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }

  }

  test("insert health data") {
    IOAssertion {
      for {
        _  <- new CountryInsertData[IO](scraperConfig, transactor).run
        _  <- new HealthInsertData[IO](transactor, parser).run(CountryCode("BI"))
      } yield ()
    }
  }

  test("find country id query") {
    check(HealthInsertStatement.findCountryId(CountryCode("AR")))
  }

  test("insert vaccine statement") {
    check(HealthInsertStatement.insertVaccine)
  }

  test("insert vaccine mandatory statement") {
    check(HealthInsertStatement.insertVaccineMandatory)
  }

  test("insert vaccine recommendations statement") {
    check(HealthInsertStatement.insertVaccineRecommendation)
  }

  test("insert vaccine optional statement") {
    check(HealthInsertStatement.insertVaccineOptional)
  }

  test("insert health alert level statement") {
    check(HealthInsertStatement.insertAlertLevel)
  }

  test("insert health alert statement") {
    check(HealthInsertStatement.insertHealthAlert)
  }

  test("insert health notice statement") {
    check(HealthInsertStatement.insertHealthNotice)
  }

}