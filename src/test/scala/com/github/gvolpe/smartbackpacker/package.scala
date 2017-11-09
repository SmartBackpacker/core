package com.github.gvolpe

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.model.CountryCode
import com.github.gvolpe.smartbackpacker.parser.AbstractWikiPageParser
import com.github.gvolpe.smartbackpacker.service.AbstractExchangeRateService
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document

import scala.io.Source

package object smartbackpacker {

  object TestWikiPageParser extends AbstractWikiPageParser[IO] {
    override def htmlDocument(from: CountryCode): IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"wikiPageTest-${from.value}.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }
  }

  object TestExchangeRateService extends AbstractExchangeRateService[IO] {
    override protected def retrieveExchangeRate(uri: String): IO[service.CurrencyExchangeDTO] = IO {
      service.CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59))
    }
  }

}
