package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.Async
import cats.instances.list._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update

class VisaRestrictionsIndexInsertData[F[_] : Async](xa: Transactor[F]) {

  type CreateVisaIndexDTO = (String, Int, Int, Int)

  private def insertVisaIndexBulk(list: List[(CountryCode, VisaRestrictionsIndex)]) = {
    val sql = "INSERT INTO visa_restrictions_index (country_code, rank, acc, sharing) VALUES (?, ?, ?, ?)"
    val dtoList: List[CreateVisaIndexDTO] = list.map { case (code, index) =>
      (code.value, index.rank.value, index.count.value, index.sharing.value)
    }
    Update[CreateVisaIndexDTO](sql).updateMany(dtoList)
  }

  def run(list: List[(CountryCode, VisaRestrictionsIndex)]): F[Unit] = {
    insertVisaIndexBulk(list).transact(xa).map(_ => ())
  }

}
