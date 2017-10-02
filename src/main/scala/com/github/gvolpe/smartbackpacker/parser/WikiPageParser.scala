package com.github.gvolpe.smartbackpacker.parser

import cats.effect.Sync
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document

object WikiPageParser {
  def apply[F[_]: Sync]: WikiPageParser[F] = new WikiPageParser[F]
}

class WikiPageParser[F[_] : Sync] extends AbstractWikiPageParser[F] {

  override def htmlDocument(from: CountryCode): Document = {
    val browser = new JsoupBrowser()
    val wikiPage = SBConfiguration.wikiPage(from).getOrElse("http://google.com")
    browser.get(wikiPage)
  }

}

abstract class AbstractWikiPageParser[F[_] : Sync] {

  def htmlDocument(from: CountryCode): Document

  def visaRequirementsFor(from: CountryCode, to: CountryName): F[VisaRequirementsFor] = Sync[F].delay {
    parseVisaRequirements(from).find(_.country == to)
      .getOrElse(VisaRequirementsFor(to, UnknownVisaCategory, "No information available"))
  }

  private def parseVisaRequirements(from: CountryCode): List[VisaRequirementsFor] = {
    val table = htmlDocument(from) >> extractor(".sortable td", texts)

    table.toList.grouped(3).map { seq =>
      VisaRequirementsFor(seq.head.asCountry, seq(1).asVisaCategory, seq(2).asDescription)
    }.toList
  }

}