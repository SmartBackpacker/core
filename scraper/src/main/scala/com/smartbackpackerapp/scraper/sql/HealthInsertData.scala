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

import cats.effect.Async
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.smartbackpackerapp.common.Log
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.scraper.model._
import com.smartbackpackerapp.scraper.parser.AbstractHealthInfoParser
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update

class HealthInsertData[F[_] : Async](xa: Transactor[F],
                                     healthInfoParser: AbstractHealthInfoParser[F])
                                    (implicit L: Log[F]) {

  private def insertVaccinationsBulk(countryId: Int, vaccines: List[Vaccine])
                                    (f: ((Int, Int)) => ConnectionIO[Int]): ConnectionIO[Int] = {
    val result = vaccines.traverse { v =>
      for {
        id <- HealthInsertStatement.insertVaccine.withUniqueGeneratedKeys[Int]("id")(v.toVaccineDTO)
        _  <- f(countryId, id)
      } yield ()
    }
    result.map(_ => vaccines.size)
  }

  private def insertHealthAlertsBulk(countryId: Int, alerts: List[HealthAlert]): ConnectionIO[Int] = {
    val result = alerts.traverse { a =>
      for {
        id <- HealthInsertStatement.insertHealthAlert.withUniqueGeneratedKeys[Int]("id")(a.toHealthAlertInsertDTO)
        _  <- HealthInsertStatement.insertHealthNotice.run((countryId, id))
      } yield ()
    }
    result.map(_ => alerts.size)
  }

  // Insert data Program
  private val errorHandler: PartialFunction[Throwable, F[Unit]] = {
    case e: HealthPageNotFound => L.error(e)
  }

  def run(cc: CountryCode): F[Unit] = {

    def statements(health: Health): ConnectionIO[(Int, Int, Int, Int)] =
      for {
        cid <- HealthInsertStatement.findCountryId(cc).unique
        rs0 <- insertVaccinationsBulk(cid, health.vaccinations.mandatory)(HealthInsertStatement.insertVaccineMandatory.run)
        rs1 <- insertVaccinationsBulk(cid, health.vaccinations.recommendations)(HealthInsertStatement.insertVaccineRecommendation.run)
        rs2 <- insertVaccinationsBulk(cid, health.vaccinations.optional)(HealthInsertStatement.insertVaccineOptional.run)
        _   <- HealthInsertStatement.insertAlertLevel.run((cid, health.notices.alertLevel.toString))
        rs3 <- insertHealthAlertsBulk(cid, health.notices.alerts)
      } yield (rs0, rs1, rs2, rs3)

    val program =
      for {
        _      <- L.info(s"${cc.value} >> Gathering health information from CDC")
        health <- healthInfoParser.parse(cc)
        _      <- L.info(s"${cc.value} >> Starting data insertion into DB")
        rs     <- statements(health).transact(xa)
        (rs0, rs1, rs2, rs3) = rs
        _      <- L.info(s"${cc.value} >> Created $rs0 records for mandatory, $rs1 for recommendations and $rs2 for optional")
        _      <- L.info(s"${cc.value} >> Created $rs3 records for health alerts")
      } yield ()

    program.recoverWith(errorHandler)
  }

}

object HealthInsertStatement {

  def findCountryId(countryCode: CountryCode): Query0[Int] = {
    sql"SELECT id FROM countries WHERE code = ${countryCode.value}"
      .query[Int]
  }

  // Vaccines
  val insertVaccine: Update[VaccineDTO] = {
    val sql = "INSERT INTO vaccine (disease, description, categories) VALUES (?, ?, ?)"
    Update[VaccineDTO](sql)
  }

  val insertVaccineMandatory: Update[(Int, Int)] = {
    val sql = "INSERT INTO vaccine_mandatory (country_id, vaccine_id) VALUES (?, ?)"
    Update[(Int, Int)](sql)
  }

  val insertVaccineRecommendation: Update[(Int, Int)] = {
    val sql = "INSERT INTO vaccine_recommendations (country_id, vaccine_id) VALUES (?, ?)"
    Update[(Int, Int)](sql)
  }

  val insertVaccineOptional: Update[(Int, Int)] = {
    val sql = "INSERT INTO vaccine_optional (country_id, vaccine_id) VALUES (?, ?)"
    Update[(Int, Int)](sql)
  }

  // Health Notices
  val insertAlertLevel: Update[(Int, String)] = {
    val sql = "INSERT INTO health_alert_level (country_id, alert_level) VALUES (?, ?)"
    Update[(Int, String)](sql)
  }

  val insertHealthAlert: Update[HealthAlertInsertDTO] = {
    val sql = "INSERT INTO health_alert (title, weblink, description) VALUES (?, ?, ?)"
    Update[HealthAlertInsertDTO](sql)
  }

  val insertHealthNotice: Update[(Int, Int)] = {
    val sql = "INSERT INTO health_notice (country_id, alert_id) VALUES (?, ?)"
    Update[(Int, Int)](sql)
  }

}
