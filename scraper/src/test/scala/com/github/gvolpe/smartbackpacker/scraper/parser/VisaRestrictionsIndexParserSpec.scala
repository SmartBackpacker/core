package com.github.gvolpe.smartbackpacker.scraper.parser

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.{FlatSpecLike, Matchers}

import scala.io.Source

class VisaRestrictionsIndexParserSpec extends FlatSpecLike with Matchers {

  object MockParser extends AbstractVisaRestrictionsIndexParser[IO] {

    override val htmlDocument: IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"visaRestrictionsWikiPageTest.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }

  }

  it should "parse visa restrictions index wiki page" in IOAssertion {
    MockParser.parse.map { result =>
      result should not be empty
      // Not all the countries are part of the ranking
      result should have size 192 // SBConfiguration.countriesCode().size == 199
    }
  }

}
