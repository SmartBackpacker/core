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

package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.Async
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.scraper.config.ScraperConfiguration
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update

class CountryInsertData[F[_] : Async](scraperConfig: ScraperConfiguration[F],
                                      xa : Transactor[F]) {

  type CountryDTO       = (String, String, String, Boolean)
  type CurrencyQueryDTO = (String, String)

  private def insertCountriesBulk(countries: List[Country]) = {
    val sql = "INSERT INTO countries (code, name, currency, schengen) VALUES (?, ?, ?, ?)"
    Update[CountryDTO](sql).updateMany(countries.map(c => (c.code.value, c.name.value, c.currency.value, false)))
  }

  private def updateCountriesCurrencyBulk(countries: List[Country]) = {
    val sql = "UPDATE countries SET currency = ? WHERE code = ?"
    Update[CurrencyQueryDTO](sql).updateMany(countries.map(c => (c.currency.value, c.code.value)))
  }

  private def updateSchengenCountriesBulk(countries: List[CountryCode]) = {
    val sql = "UPDATE countries SET schengen = 't' WHERE code = ?"
    Update[String](sql).updateMany(countries.map(_.value))
  }

  private def runSchengenUpdate: F[Unit] = {
    scraperConfig.schengen() flatMap { countries =>
      updateSchengenCountriesBulk(countries).transact(xa).map(_ => ())
    }
  }

  private def runCurrencyUpdate: F[Unit] = {
    scraperConfig.countries() flatMap { countries =>
      updateCountriesCurrencyBulk(countries).transact(xa).map(_ => ())
    }
  }

  def runUpdate: F[Unit] = {
    runSchengenUpdate.flatMap(_ => runCurrencyUpdate)
  }

  def run: F[Unit] = {
    scraperConfig.countries() flatMap { countries =>
      insertCountriesBulk(countries).transact(xa).map(_ => ())
    }
  }

}
