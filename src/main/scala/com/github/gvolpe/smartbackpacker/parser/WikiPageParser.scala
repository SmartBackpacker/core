package com.github.gvolpe.smartbackpacker.parser

import cats.effect.Sync
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document

object WikiPageParser {
  def apply[F[_]: Sync]: WikiPageParser[F] = new WikiPageParser[F]()
}

class WikiPageParser[F[_] : Sync] {

  def visaRequirementsFor(from: CountryCode, to: CountryCode): F[VisaRequirements] = Sync[F].delay {
    val countryCode = if (to == "Viet Nam") "Vietnam" else to.toLowerCase().capitalize
    parseVisaRequirements(from).find(_.country == countryCode).get
  }

  // TODO: Make this request asynchronous
  private def htmlDocument(from: CountryCode): Document = {
    val browser = new JsoupBrowser()
    val wikiPage = SBConfiguration.wikiPage("AR").getOrElse("http://google.com")
    browser.get(wikiPage)
  }

  private def parseVisaRequirements(from: CountryCode): List[VisaRequirements] = {
    val table = htmlDocument(from) >> extractor(".sortable td", texts)

    table.toList.grouped(3).map { seq =>
      VisaRequirements(seq.head.asCountry, seq(1).asVisaCategory, seq(2).asDescription)
    }.toList
  }

}