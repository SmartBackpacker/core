package com.github.gvolpe.smartbackpacker.scraper.parser

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.{FlatSpecLike, Matchers}

import scala.io.Source

class HealthInfoParserSpec extends FlatSpecLike with Matchers {

  object MockParser extends AbstractHealthInfoParser[IO] {

    override def htmlDocument(from: CountryCode): IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"healthInfo-${from.value}.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }

  }

  it should "parse health information page" in IOAssertion {
    val from = new CountryCode("AR")
    MockParser.parse(from).map( result =>
      println(result)
    )
  }

}
