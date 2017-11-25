package com.github.gvolpe.smartbackpacker.scraper.parser

import cats.effect.IO
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

}
