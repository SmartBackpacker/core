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
import com.smartbackpackerapp.model.{CountryCode, LevelOne, LevelTwo}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.{FlatSpecLike, Matchers}

import scala.io.Source

class HealthInfoParserSpec extends FlatSpecLike with Matchers {

  private val parser = new AbstractHealthInfoParser[IO] {

    override def htmlDocument(from: CountryCode): IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"healthInfo-${from.value}.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }

  }

  it should "parse health information page for AR (Argentina)" in IOAssertion {
    parser.parse(CountryCode("AR")).map { health =>
      health.vaccinations.mandatory       should be (empty)
      health.vaccinations.recommendations should have size 2
      health.vaccinations.optional        should have size 3
      health.notices.alertLevel           should be (LevelTwo)
      health.notices.alerts               should have size 1
      health.notices.alerts.foreach(_.link.value should not be empty)
    }
  }

  it should "parse health information page for BI (Burundi)" in IOAssertion {
    parser.parse(CountryCode("BI")).map { health =>
      health.vaccinations.mandatory       should have size 1
      health.vaccinations.recommendations should have size 3
      health.vaccinations.optional        should have size 3
      health.notices.alertLevel           should be (LevelOne)
      health.notices.alerts               should have size 1
      health.notices.alerts.foreach(_.link.value should not be empty)
    }
  }

  it should "parse health information page for AG (Antigua and Barbuda)" in IOAssertion {
    parser.parse(CountryCode("AG")).map { health =>
      health.vaccinations.mandatory       should be (empty)
      health.vaccinations.recommendations should have size 2
      health.vaccinations.optional        should have size 3
      health.notices.alertLevel           should be (LevelTwo)
      health.notices.alerts               should have size 2
      health.notices.alerts.foreach(_.link.value should not be empty)
    }
  }


}
