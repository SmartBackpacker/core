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
import com.smartbackpackerapp.scraper.model.VisaRestrictionsRanking
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor

import scala.util.{Failure, Success, Try}

class VisaRestrictionsIndexParser[F[_]](scraperConfig: ScraperConfiguration[F])
                                       (implicit F: Sync[F]) extends AbstractVisaRestrictionsIndexParser[F](scraperConfig) {

  override val htmlDocument: F[Document] = F.delay {
    val browser = new JsoupBrowser()
    browser.get("https://en.wikipedia.org/wiki/Travel_visa")
  }

}

abstract class AbstractVisaRestrictionsIndexParser[F[_]](scraperConfig: ScraperConfiguration[F])
                                                        (implicit F: Monad[F]) {

  private val CountriesOnIndex: Int = 104 // Number of countries that are part of the ranking

  def htmlDocument: F[Document]

  private val countryNames: F[List[(CountryCode, List[String])]] =
    for {
      ccs <- scraperConfig.countriesCode()
      nms <- ccs.traverse { cc => scraperConfig.countryNames(cc).map((cc, _)) }
    } yield nms

  def parse: F[List[(CountryCode, VisaRestrictionsIndex)]] = {
    countryNames flatMap { codeAndNames =>
      htmlDocument.map { doc =>
        val wikiTable: List[Element] = doc >> elementList(".sortable")
        val result = wikiTable.flatMap(e => (e >> extractor(".collapsible td", wikiTableExtractor)).toList)

        val ranking = result.grouped(3).take(CountriesOnIndex).map {
          case List(Rank(r), Countries(c), PlacesCount(pc)) => VisaRestrictionsRanking(r, c, pc)
        }.toList

        for {
          cn            <- codeAndNames
          (code, names) = cn
          index         <- ranking
          country       <- index.countries
          if names.contains(country)
        } yield {
          val visaIndex = VisaRestrictionsIndex(
            rank = Ranking(index.rank),
            count = Count(index.count),
            sharing = Sharing(index.countries.size)
          )
          (code, visaIndex)
        }
      }
    }
  }

  private val wikiTableExtractor: HtmlExtractor[Element, Iterable[VisaRestrictionsIndexValues]] = _.map { e =>
    Try(e.text.toInt) match {
      case Success(n) =>
        if (e.innerHtml.contains("#ffc90e")) PlacesCount(n)
        else Rank(n)
      case Failure(_) =>
        val countries = e.text.split(",").toList.map(_.trim.noWhiteSpaces)
        Countries(countries)
    }
  }

}
