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

package com.smartbackpackerapp.repository

import cats.Monad
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.repository.algebra.CountryRepository
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor

class PostgresCountryRepository[F[_] : Monad](xa: Transactor[F]) extends CountryRepository[F] {

  private def findCountries(query: ConnectionIO[List[CountryDTO]]) = {
    query.map(_.map(_.toCountry)).transact(xa)
  }

  override def findAll: F[List[Country]] = {
    findCountries(CountryStatement.findCountries.to[List])
  }

  override def findSchengen: F[List[Country]] = {
    findCountries(CountryStatement.findSchengen.to[List])
  }

}

object CountryStatement {

  val findCountries: Query0[CountryDTO] = {
    sql"SELECT id, code, name, currency FROM countries ORDER BY name"
      .query[CountryDTO]
  }

  val findSchengen: Query0[CountryDTO] = {
    sql"SELECT id, code, name, currency FROM countries WHERE schengen ORDER BY name"
      .query[CountryDTO]
  }

}