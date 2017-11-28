package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.Async
import cats.instances.list._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update

import scala.reflect.runtime.{universe => ru}

class VisaCategoryInsertData[F[_] : Async](xa : Transactor[F]) {

  private def insertVisaCategoriesBulk(categories: List[String])  = {
    val sql = "INSERT INTO visa_category (name) VALUES (?)"
    Update[String](sql).updateMany(categories)
  }

  private def visaCategories: List[String] = {
    val tpe   = ru.typeOf[VisaCategory]
    val clazz = tpe.typeSymbol.asClass
    clazz.knownDirectSubclasses.map(_.name.toString).toList
  }

  def run: F[Unit] = {
    insertVisaCategoriesBulk(visaCategories).transact(xa).map(_ => ())
  }

}
