package com.github.gvolpe.smartbackpacker.scraper.sql

import java.sql.BatchUpdateException

import cats.effect.Async
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.scraper.parser.AbstractVisaRequirementsParser
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update

class VisaRequirementsInsertData[F[_]](xa: Transactor[F],
                                       visaRequirementsParser: AbstractVisaRequirementsParser[F])
                                      (implicit F: Async[F]) {

  private def insertVisaRequirementsBulk(list: List[VisaRequirementsFor]) = {
    val sql =
      """
        |WITH from_view AS (
        |  SELECT id AS from_id FROM countries WHERE code = ?
        |),
        |to_view AS (
        |  SELECT id AS to_id FROM countries WHERE code = ?
        |),
        |visa_cat_view AS (
        |  SELECT id AS visa_id FROM visa_category WHERE name = ?
        |),
        |desc_view AS (
        |  SELECT ? AS description
        |)
        |INSERT INTO visa_requirements (from_country, to_country, visa_category, description)
        |SELECT from_id, to_id, visa_id, description FROM from_view, to_view, visa_cat_view, desc_view
      """.stripMargin
    Update[VisaRequirementsDTO](sql).updateMany(list.map(_.toVisaRequirementsDTO))
  }

  private val errorHandler: PartialFunction[Throwable, F[Unit]] = {
    // For example Algerian Wiki page has Burundi duplicated
    case e: BatchUpdateException if e.getCause.getMessage.contains("duplicate key value") =>
      F.delay(println(e.getMessage))
    case WikiPageNotFound(code) =>
      F.delay(println(s"Wiki page not found for $code"))
  }

  def run(from: CountryCode): F[Unit] = {
    val program = for {
      _   <- F.delay(println(s"${from.value} >> Gathering visa requirements from Wikipedia"))
      req <- visaRequirementsParser.visaRequirementsFor(from)
      _   <- F.delay(println(s"${from.value} >> Starting data insertion into DB"))
      rs  <- insertVisaRequirementsBulk(req).transact(xa)
      _   <- F.delay(println(s"${from.value} >> Created $rs records"))
    } yield ()
    program.recoverWith(errorHandler)
  }

}
