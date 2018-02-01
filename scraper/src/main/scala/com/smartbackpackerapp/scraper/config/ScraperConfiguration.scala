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

package com.smartbackpackerapp.scraper.config

import cats.effect.Sync
import com.smartbackpackerapp.config.SafeConfigReader
import com.smartbackpackerapp.model.{Country, CountryCode, CountryName, CountryWithNames, Currency}
import com.typesafe.config.ConfigFactory

class ScraperConfiguration[F[_]](implicit F: Sync[F]) {

  private lazy val configuration  = ConfigFactory.load("sb-scraper")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def wikiPage(countryCode: CountryCode): F[Option[String]] = F.delay {
    safeConfig.string(s"visa-requirements.page.${countryCode.value}")
  }

  def healthPage(countryCode: CountryCode): F[Option[String]] = F.delay {
    safeConfig.string(s"health.page.${countryCode.value}")
  }

  def schengen(): F[List[CountryCode]] = F.delay {
    safeConfig.list("countries.schengen").map(CountryCode.apply)
  }

  def countries(): F[List[Country]] = F.delay {
    val names = safeConfig.objectMapOfList("countries.name").map {
      case ((co, ns)) => (CountryCode(co), CountryName(ns.headOption.getOrElse("Empty")))
    }.toList.sortBy(_._1.value)

    val currencies = safeConfig.objectMap("countries.currency").map {
      case ((co, cu)) => (CountryCode(co), Currency(cu))
    }.toList.sortBy(_._1.value)

    names.zip(currencies).flatMap {
      case ((co, cn), (co2, cu)) if co.value == co2.value =>
        List(Country(co, cn, cu))
      case _ => List.empty[Country]
    }.sortBy(_.code.value)
  }

  def countriesWithNames(): F[List[CountryWithNames]] = F.delay {
    safeConfig.objectMapOfList("countries.name").map {
      case ((co, ns)) => CountryWithNames(CountryCode(co), ns.map(CountryName.apply))
    }.toList.sortBy(_.code.value)
  }

  def countriesCode(): F[List[CountryCode]] = F.delay {
    safeConfig.objectKeyList("countries.name").sorted.map(CountryCode.apply)
  }

  def countryNames(countryCode: CountryCode): F[List[String]] = F.delay {
    safeConfig.list(s"countries.name.${countryCode.value}")
  }

}
