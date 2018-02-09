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
import com.smartbackpackerapp.model.CountryCode
import com.smartbackpackerapp.scraper.config.ScraperConfiguration
import com.smartbackpackerapp.scraper.parser.AbstractVisaRequirementsParser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document

import scala.io.Source

class VisaRequirementsInsertDataSpec extends RepositorySpec {

  override def testDbName: String = getClass.getSimpleName

  private val scraperConfig = new ScraperConfiguration[IO]

  private val parser = new AbstractVisaRequirementsParser[IO](scraperConfig) {
    override def htmlDocument(from: CountryCode): IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"wikiPageTest-${from.value}.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }
  }

  // H2 does not support the complex SQL syntax used in this query
  ignore("insert visa requirements data") {
    IOAssertion {
      for {
        _  <- new CountryInsertData[IO](scraperConfig, transactor).run
        _  <- new VisaCategoryInsertData[IO](transactor).run
        _  <- new VisaRequirementsInsertData[IO](transactor, parser).run(CountryCode("AR"))
      } yield ()
    }
  }

  // H2 does not support the complex SQL syntax used in this query
  ignore("visa requirements insert statement") {
    check(VisaRequirementsInsertStatement.insertVisaRequirements)
  }

}