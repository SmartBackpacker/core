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

package com.smartbackpackerapp.scraper.parser

import cats.effect.IO
import com.smartbackpackerapp.common.IOAssertion
import com.smartbackpackerapp.scraper.config.ScraperConfiguration
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.{FlatSpecLike, Matchers}

import scala.io.Source

class VisaRestrictionsIndexParserSpec extends FlatSpecLike with Matchers {

  private val scraperConfig = new ScraperConfiguration[IO]

  private val parser = new AbstractVisaRestrictionsIndexParser[IO](scraperConfig) {

    override val htmlDocument: IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource("passportRanking2018.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }

  }

  it should "parse visa restrictions index 2018 wiki page" in IOAssertion {
    parser.parse.map { result =>
      result should not be empty
      // Not all the countries are part of the ranking
      result should have size 194 // SBConfiguration.countriesCode().size == 199

      result.foreach(println)
    }
  }

}
