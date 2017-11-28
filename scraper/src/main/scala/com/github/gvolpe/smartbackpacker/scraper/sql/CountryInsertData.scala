package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.Async
import cats.instances.list._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update

class CountryInsertData[F[_] : Async](xa : Transactor[F]) {

  type CountryDTO = (String, String)

  private def insertCountriesBulk(countries: List[Country])  = {
    val sql = "INSERT INTO countries (code, name) VALUES (?, ?)"
    Update[CountryDTO](sql).updateMany(countries.map(c => (c.code.value, c.name.value)))
  }

  def run: F[Unit] = {
    val countries = SBConfiguration.countries()
    insertCountriesBulk(countries).transact(xa).map(_ => ())
  }

}
