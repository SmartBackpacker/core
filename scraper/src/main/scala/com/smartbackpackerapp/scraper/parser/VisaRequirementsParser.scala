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

import cats.Monad
import cats.effect.Sync
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.scraper.config.ScraperConfiguration
import com.smartbackpackerapp.scraper.model._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor

import scala.util.{Failure, Success, Try}

class VisaRequirementsParser[F[_]](scraperConfig: ScraperConfiguration[F])
                                  (implicit F: Sync[F]) extends AbstractVisaRequirementsParser[F](scraperConfig) {

  override def htmlDocument(from: CountryCode): F[Document] = {
    val ifEmpty: F[Document] = F.raiseError(WikiPageNotFound(from.value))

    scraperConfig.wikiPage(from) flatMap { maybeWikiPage =>
      maybeWikiPage.fold(ifEmpty) { wikiPage =>
        F.delay {
          val browser = new JsoupBrowser()
          browser.get(wikiPage)
        }
      }
    }
  }

}

abstract class AbstractVisaRequirementsParser[F[_]](scraperConfig: ScraperConfiguration[F])
                                                   (implicit F: Monad[F]) {

  def htmlDocument(from: CountryCode): F[Document]

  def visaRequirementsFor(from: CountryCode): F[List[VisaRequirementsFor]] = {
    parseVisaRequirements(from) flatMap { vrp =>
      vrp.traverse { vr =>
        // If we can't find the code we just use the same the country name instead to easily trace the data
        val ifEmpty = VisaRequirementsFor(from, CountryCode(vr.to.value), vr.visaCategory, vr.description)
        countryCodeFor(vr.to).map(_.fold(ifEmpty)(VisaRequirementsFor(from, _, vr.visaCategory, vr.description)))
      }
    }
  }

  private def countryCodeFor(name: CountryName): F[Option[CountryCode]] = {
    scraperConfig.countriesWithNames().map(_.find(_.names.map(_.value).contains(name.value)).map(_.code))
  }

  // To handle special cases like the Irish wiki page containing a table of both 4 and 5 columns
  private val wikiTableExtractor: HtmlExtractor[Element, Iterable[String]] = _.flatMap { e =>
    val sortTextSpan  = e >?> elementList(".sorttext")
    val sortText      = sortTextSpan.flatMap(_.headOption).map(_.text)
    val text          = sortText.getOrElse(e.text.split('!').head.trim) // for cases like Ivory Coast

    Try(e.attr("colspan")) match {
      case Success(cs) if cs == "2" => Seq(text, "")
      case _                        => Seq(text)
    }
  }

  private val colspanExtractor: HtmlExtractor[Element, Option[Element]] = _.find { e =>
    Try(e.attr("colspan")) match {
      case Success(cs) if cs == "2" => true
      case Success(_)               => true
      case Failure(_)               => false
    }
  }

  private val normalTableMapper: List[String] => VisaRequirementsParsing = {
    case (c :: v :: d :: xt :: x :: Nil) =>
      val extra = if (x == "X" || x == "√") xt else xt + " " + x
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), v.asVisaCategory, d.asDescription(extra))
    case (c :: v :: d :: x :: Nil) =>
      val extra = if (x == "X" || x == "√") "" else x
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), v.asVisaCategory, d.asDescription(extra))
    case (c :: v :: d :: Nil) =>
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), v.asVisaCategory, d.asDescription(""))
    case (c :: v :: Nil) =>
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), v.asVisaCategory, "")
    case (c :: Nil) =>
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), UnknownVisaCategory, "")
    case _ =>
      VisaRequirementsParsing(CountryName("Not Found"), UnknownVisaCategory, "")
  }

  private val colspanTableMapper: List[String] => VisaRequirementsParsing = {
    case (c :: v :: d :: x :: _ :: Nil) =>
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), v.asVisaCategory, d.asDescription(x))
    case (c :: v :: d :: x :: Nil) =>
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), v.asVisaCategory, d.asDescription(x))
    case (c :: v :: d :: Nil) =>
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), v.asVisaCategory, d.asDescription(""))
    case (c :: v :: Nil) =>
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), v.asVisaCategory, "")
    case (c :: Nil) =>
      VisaRequirementsParsing(CountryName(c.noWhiteSpaces), UnknownVisaCategory, "")
    case _ =>
      VisaRequirementsParsing(CountryName("Not Found"), UnknownVisaCategory, "")
  }

  private val parseColumns: Element => List[String] = e => {
    e.children.toList.flatMap(_ >> extractor("td", wikiTableExtractor))
  }

  private val rowspanMapper: Int => List[Element] => List[String] = tableSize => {
    case (x :: y :: xs) =>
      if (x.children.exists(_.hasAttr("rowspan"))) {
        val first  = parseColumns(x)
        val second = parseColumns(y)
        val lastCombined = first.lastOption.toList.map(_ ++ " " ++ second.mkString)
        // Drop the last element and append the combined one
        (first.dropRight(1) ::: lastCombined) ::: rowspanMapper(tableSize)(xs)
      } else {
        parseColumns(x) match {
          case Nil => Nil ::: rowspanMapper(tableSize)(y :: xs)
          case ys if ys.lengthCompare(tableSize) == 0 => ys ::: rowspanMapper(tableSize)(y :: xs)
          case ys if ys.lengthCompare(tableSize) > 0 => ys.dropRight(1) ::: rowspanMapper(tableSize)(y :: xs) // See Marshal Islands in Brazil visa requirements
          case ys => ys ::: "" :: rowspanMapper(tableSize)(y :: xs) // See Iceland in US visa requirements
        }
      }
    case (x :: xs) =>
      val noRowspan = parseColumns(x)
      noRowspan ::: rowspanMapper(tableSize)(xs)
    case _ =>
      List.empty[String]
  }

  // TODO: Aggregate ".sortable" table with ".wikitable" table that for some countries have partially recognized countries like Kosovo
  // TODO: This will require add more visa categories (See Polish page)
  private def parseVisaRequirements(from: CountryCode): F[List[VisaRequirementsParsing]] =
    htmlDocument(from) map { doc =>
      // Get first of all sortable tables
      val wikiTables = (doc >> elementList(".sortable")).headOption
      // Find out whether it's an irregular (colspan=2) or regular table
      val colspan = wikiTables.flatMap(_ >> extractor(".sortable td", colspanExtractor))
      // Find out the number of columns
      val tableSize = wikiTables.map { e => (e >> extractor(".sortable th", texts)).size }
      // Extract all the rows with visa information
      val table = wikiTables.toList.flatMap(_ >> extractor(".sortable tr", elementList))
      // Parse every row, extract the field and combine them whenever there's a rowspan
      val info  = rowspanMapper(tableSize.getOrElse(3))(table)
      // Group it per country using the corresponding mapper
      val mapper = colspan.fold(normalTableMapper)(_ => colspanTableMapper)
      info.grouped(tableSize.getOrElse(3)).map(mapper).toList
    }

}
