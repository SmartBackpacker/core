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

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.option._
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.repository.algebra.HealthRepository
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import doobie.util.query.Query0
import doobie.util.transactor.Transactor

class PostgresHealthRepository[F[_]](xa: Transactor[F])
                                    (implicit F: MonadError[F, Throwable]) extends HealthRepository[F] {

  override def findHealthInfo(from: CountryCode): F[Option[Health]] = {
    val program: ConnectionIO[Health] =
      for {
        c <- HealthStatement.findCountryId(from).unique
        m <- HealthStatement.mandatory(c).to[List]
        r <- HealthStatement.recommendations(c).to[List]
        o <- HealthStatement.optional(c).to[List]
        n <- HealthStatement.healthNotices(c).to[List]
        a <- HealthStatement.healthAlert(c).unique
      } yield {
        val mandatory       = m.map(_.toVaccine)
        val recommendations = r.map(_.toVaccine)
        val optional        = o.map(_.toVaccine)
        val vaccinations    = Vaccinations(mandatory, recommendations, optional)
        val alertLevel      = a.toAlertLevel
        val healthNotices   = n.map(_.toHealthAlert)
        Health(vaccinations, HealthNotices(alertLevel, healthNotices))
      }

    program.map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[Health].pure[F]
    }
  }

}

object HealthStatement {

  def findCountryId(from: CountryCode): Query0[Int] = {
    sql"SELECT id FROM countries WHERE code = ${from.value}"
      .query[Int]
  }

  def mandatory(countryId: Int): Query0[VaccineDTO] = {
    sql"SELECT v.disease, v.description, v.categories FROM vaccine_mandatory AS vm INNER JOIN vaccine AS v ON vm.vaccine_id=v.id WHERE vm.country_id = $countryId"
      .query[VaccineDTO]
  }

  def recommendations(countryId: Int): Query0[VaccineDTO] = {
    sql"SELECT v.disease, v.description, v.categories FROM vaccine_recommendations AS vr INNER JOIN vaccine AS v ON vr.vaccine_id=v.id WHERE vr.country_id = $countryId"
      .query[VaccineDTO]
  }

  def  optional(countryId: Int): Query0[VaccineDTO] = {
    sql"SELECT v.disease, v.description, v.categories FROM vaccine_optional AS vo INNER JOIN vaccine AS v ON vo.vaccine_id=v.id WHERE vo.country_id = $countryId"
      .query[VaccineDTO]
  }

  def healthNotices(countryId: Int): Query0[HealthNoticeDTO] = {
    sql"SELECT ha.title, ha.weblink, ha.description FROM health_notice AS hn INNER JOIN health_alert AS ha ON hn.alert_id = ha.id WHERE hn.country_id = $countryId"
      .query[HealthNoticeDTO]
  }

  def healthAlert(countryId: Int): Query0[HealthAlertDTO] = {
    sql"SELECT alert_level FROM health_alert_level WHERE country_id = $countryId"
      .query[HealthAlertDTO]
  }

}
