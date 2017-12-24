package com.github.gvolpe.smartbackpacker.scraper.parser

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model._
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
    parser.parse("AR".as[CountryCode]).map { health =>
      health.vaccinations.mandatory       should be (empty)
      health.vaccinations.recommendations should have size 2
      health.vaccinations.optional        should have size 3
      health.notices.alertLevel           should be (LevelTwo)
      health.notices.alerts               should have size 1
      health.notices.alerts.foreach(_.link.value should not be empty)
    }
  }

  it should "parse health information page for BI (Burundi)" in IOAssertion {
    parser.parse("BI".as[CountryCode]).map { health =>
      health.vaccinations.mandatory       should have size 1
      health.vaccinations.recommendations should have size 3
      health.vaccinations.optional        should have size 3
      health.notices.alertLevel           should be (LevelOne)
      health.notices.alerts               should have size 1
      health.notices.alerts.foreach(_.link.value should not be empty)
    }
  }

  it should "parse health information page for AG (Antigua and Barbuda)" in IOAssertion {
    parser.parse("AG".as[CountryCode]).map { health =>
      health.vaccinations.mandatory       should be (empty)
      health.vaccinations.recommendations should have size 2
      health.vaccinations.optional        should have size 3
      health.notices.alertLevel           should be (LevelTwo)
      health.notices.alerts               should have size 2
      health.notices.alerts.foreach(_.link.value should not be empty)
    }
  }


}
