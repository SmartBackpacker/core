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

package com.smartbackpackerapp.scraper.sql

import java.sql.BatchUpdateException

import cats.effect.Async
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.smartbackpackerapp.common.Log
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.scraper.model._
import com.smartbackpackerapp.scraper.parser.AbstractVisaRequirementsParser
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update

class VisaRequirementsInsertData[F[_] : Async](xa: Transactor[F],
                                               visaRequirementsParser: AbstractVisaRequirementsParser[F])
                                              (implicit L: Log[F]) {

  private def insertVisaRequirementsBulk(list: List[VisaRequirementsFor]) = {
    VisaRequirementsInsertStatement.insertVisaRequirements
      .updateMany(list.map(_.toVisaRequirementsDTO))
  }

  // For example Algerian Wiki page has Burundi duplicated
  private val errorHandler: PartialFunction[Throwable, F[Unit]] = {
    case e: BatchUpdateException if e.getCause.getMessage.contains("duplicate key value") => L.error(e)
    case e: WikiPageNotFound => L.error(e)
  }

  def run(from: CountryCode): F[Unit] = {
    val program = for {
      _   <- L.info(s"${from.value} >> Gathering visa requirements from Wikipedia")
      req <- visaRequirementsParser.visaRequirementsFor(from)
      _   <- L.info(s"${from.value} >> Starting data insertion into DB")
      rs  <- insertVisaRequirementsBulk(req).transact(xa)
      _   <- L.info(s"${from.value} >> Created $rs records")
    } yield ()
    program.recoverWith(errorHandler)
  }

}

object VisaRequirementsInsertStatement {

  val insertVisaRequirements: Update[VisaRequirementsDTO] = {
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
    Update[VisaRequirementsDTO](sql)
  }

}
