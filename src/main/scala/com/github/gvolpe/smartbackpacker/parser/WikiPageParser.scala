package com.github.gvolpe.smartbackpacker.parser

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor

import scala.util.{Failure, Success, Try}

object WikiPageParser {
  def apply[F[_]: Effect]: WikiPageParser[F] = new WikiPageParser[F]
}

class WikiPageParser[F[_] : Effect] extends AbstractWikiPageParser[F] {

  override def htmlDocument(from: CountryCode): F[Document] = Effect[F].delay {
    val browser = new JsoupBrowser()
    val wikiPage = SBConfiguration.wikiPage(from).getOrElse("http://google.com")
    browser.get(wikiPage)
  }

}

abstract class AbstractWikiPageParser[F[_] : Effect] {

  def htmlDocument(from: CountryCode): F[Document]

  def visaRequirementsFor(from: CountryCode, to: CountryName): F[VisaRequirementsFor] =
    Effect[F].map(parseVisaRequirements(from)) { requirements =>
      requirements.find(_.country == to.value)
        .getOrElse(VisaRequirementsFor(to.value, UnknownVisaCategory, "No information available"))
    }

  // To handle special cases like the Irish wiki page containing a table of both 4 and 5 columns
  private def wikiTableExtractor(from: CountryCode): HtmlExtractor[Element, Iterable[String]] = _.flatMap { e =>
    val sortTextSpan  = e >?> elementList(".sorttext")
    val sortText      = sortTextSpan.flatMap(_.headOption).map(_.text)
    val text          = sortText.getOrElse(e.text.split('!').head.trim) // for cases like Ivory Coast

    Try(e.attr("colspan")) match {
      case Success(cs) if cs == "2" => Seq(text, "")
      case Success(_)               => Seq(text)
      case Failure(_)               => Seq(text)
    }
  }

  private val colspanExtractor: HtmlExtractor[Element, Option[Element]] = _.find { e =>
    Try(e.attr("colspan")) match {
      case Success(cs) if cs == "2" => true
      case Success(_)               => true
      case Failure(_)               => false
    }
  }

  private val normalTableMapper: List[String] => VisaRequirementsFor = {
    case (c :: v :: d :: x :: Nil) =>
      val extra = if (x == "X" || x == "√") "" else x
      VisaRequirementsFor(c.asCountryName, v.asVisaCategory, d.asDescription(extra))
    case (c :: v :: d :: Nil) =>
      VisaRequirementsFor(c.asCountryName, v.asVisaCategory, d.asDescription(""))
    case (c :: v :: Nil) =>
      VisaRequirementsFor(c.asCountryName, v.asVisaCategory, "")
    case (c :: Nil) =>
      VisaRequirementsFor(c.asCountryName, UnknownVisaCategory, "")
    case _ =>
      VisaRequirementsFor("Not Found", UnknownVisaCategory, "")
  }

  private val colspanTableMapper: List[String] => VisaRequirementsFor = {
    case (c :: v :: d :: x :: _ :: Nil) =>
      VisaRequirementsFor(c.asCountryName, v.asVisaCategory, d.asDescription(x))
    case (c :: v :: d :: x :: Nil) =>
      VisaRequirementsFor(c.asCountryName, v.asVisaCategory, d.asDescription(x))
    case (c :: v :: d :: Nil) =>
      VisaRequirementsFor(c.asCountryName, v.asVisaCategory, d.asDescription(""))
    case (c :: v :: Nil) =>
      VisaRequirementsFor(c.asCountryName, v.asVisaCategory, "")
    case (c :: Nil) =>
      VisaRequirementsFor(c.asCountryName, UnknownVisaCategory, "")
    case _ =>
      VisaRequirementsFor("Not Found", UnknownVisaCategory, "")
  }

  // TODO: Aggregate ".sortable" table with ".wikitable" table that for some countries have partially recognized countries like Kosovo
  // TODO: This will require add more visa categories (See Polish page)
  private def parseVisaRequirements(from: CountryCode): F[List[VisaRequirementsFor]] =
    Effect[F].map(htmlDocument(from)) { doc =>
      // Get first of all sortable tables
      val wikiTables = (doc >> elementList(".sortable")).headOption
      // Find out whether it's an irregular (colspan=2) or regular table
      val colspan = wikiTables.flatMap(_ >> extractor(".sortable td", colspanExtractor))
      // Find out the number of columns
      val tableSize = wikiTables.map { e => (e >> extractor(".sortable th", texts)).size }
      // Extract all the rows with visa information
      val table = wikiTables.toList.flatMap(_ >> extractor(".sortable tr", elementList))
      // Parse every row, extract the field and combine them whenever there's a rowspan
      val info  = rowspanMapper(from)(table)

      // Group it per country using the corresponding mapper
      val mapper = colspan.fold(normalTableMapper)(_ => colspanTableMapper)
      val result = info.grouped(tableSize.getOrElse(3)).map(mapper).toList
      result
    }

  private val parseColumns: Element => CountryCode => List[String] = e => cc => {
    e.children.toList.flatMap(_ >> extractor("td", wikiTableExtractor(cc)))
  }

  private val rowspanMapper: CountryCode => List[Element] => List[String] = from => {
    case (x :: y :: xs) =>
      if (x.children.exists(_.hasAttr("rowspan"))) {
        val first  = parseColumns(x)(from)
        val second = parseColumns(y)(from)
        val lastCombined = first.lastOption.toList.map(_ ++ " " ++ second.mkString)
        // Drop the last element and append the combined one
        (first.dropRight(1) ::: lastCombined) ::: rowspanMapper(from)(xs)
      } else {
        val noRowspan = parseColumns(x)(from)
        noRowspan ::: rowspanMapper(from)(y :: xs)
      }
    case (x :: xs) =>
      val noRowspan = parseColumns(x)(from)
      noRowspan ::: rowspanMapper(from)(xs)
    case _ =>
      List.empty[String]
  }

}