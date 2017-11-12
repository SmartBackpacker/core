package com.github.gvolpe.smartbackpacker.parser

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.model._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor

import scala.util.{Failure, Success, Try}

object VisaRestrictionsIndexParser {
  def apply[F[_] : Effect]: VisaRestrictionsIndexParser[F] = new VisaRestrictionsIndexParser[F]()
}

class VisaRestrictionsIndexParser[F[_] : Effect] extends AbstractVisaRestrictionsIndexParser[F] {

  override val htmlDocument: F[Document] = Effect[F].delay {
    val browser = new JsoupBrowser()
    browser.get("https://en.wikipedia.org/wiki/Travel_visa")
  }

}

abstract class AbstractVisaRestrictionsIndexParser[F[_] : Effect] {

  val htmlDocument: F[Document]

  def parse: F[List[VisaRestrictionIndex]] = {
    Effect[F].map(htmlDocument) { doc =>
      val wikiTable: List[Element] = doc >> elementList(".sortable")
      val result = wikiTable.flatMap(e => (e >> extractor(".collapsible td", wikiTableExtractor)).toList)
      result.grouped(3).take(104).map {
        case List(Rank(r), Countries(c), PlacesCount(pc)) => VisaRestrictionIndex(r, c, pc)
      }.toList
    }
  }

  private val wikiTableExtractor: HtmlExtractor[Element, Iterable[VisaRestrictionsIndexValues]] = _.map { e =>
    Try(e.text.toInt) match {
      case Success(n) =>
        if (e.innerHtml.contains("#199502")) PlacesCount(n)
        else Rank(n)
      case Failure(_) =>
        val countries = e.text.split(",").toList.map(_.trim.noWhiteSpaces)
        Countries(countries)
    }
  }

}
