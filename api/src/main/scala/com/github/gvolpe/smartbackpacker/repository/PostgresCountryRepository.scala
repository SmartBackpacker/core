package com.github.gvolpe.smartbackpacker.repository

import cats.Monad
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.CountryRepository
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

class PostgresCountryRepository[F[_] : Monad](xa: Transactor[F]) extends CountryRepository[F] {

  private def findCountries(query: ConnectionIO[List[CountryDTO]]) = {
    query.map(_.map(_.toCountry)).transact(xa)
  }

  override def findAll: F[List[Country]] = {
    val sql = sql"SELECT id, code, name, currency FROM countries ORDER BY name"
      .query[CountryDTO].list
    findCountries(sql)
  }

  override def findSchengen: F[List[Country]] = {
    val sql = sql"SELECT id, code, name, currency FROM countries WHERE schengen ORDER BY name"
      .query[CountryDTO].list
    findCountries(sql)
  }

}