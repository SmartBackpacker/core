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
import cats.syntax.option.none
import com.smartbackpackerapp.model.{CountryCode, VisaRequirementsData}
import com.smartbackpackerapp.repository.algebra.VisaRequirementsRepository
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import doobie.util.query.Query0
import doobie.util.transactor.Transactor

class PostgresVisaRequirementsRepository[F[_]](xa: Transactor[F])
                                              (implicit F: MonadError[F, Throwable]) extends VisaRequirementsRepository[F] {

  override def findVisaRequirements(from: CountryCode, to: CountryCode): F[Option[VisaRequirementsData]] = {
    val program: ConnectionIO[VisaRequirementsData] =
      for {
        f <- VisaRequirementsStatement.from(from).unique
        t <- VisaRequirementsStatement.to(to).unique
        v <- VisaRequirementsStatement.visaRequirements(f.head, t.head).unique
      } yield v.toVisaRequirementsData(f, t)

    program.map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[VisaRequirementsData].pure[F]
    }
  }

}

object VisaRequirementsStatement {

  def from(from: CountryCode): Query0[CountryDTO] = {
    sql"SELECT id, code, name, currency FROM countries WHERE code = ${from.value}"
      .query[CountryDTO]
  }

  def to(to: CountryCode): Query0[CountryDTO] = {
    sql"SELECT id, code, name, currency FROM countries WHERE code = ${to.value}"
      .query[CountryDTO]
  }

  def visaRequirements(idFrom: Int, idTo: Int): Query0[VisaRequirementsDTO] =
    sql"SELECT vc.name AS category, vr.description FROM visa_requirements AS vr INNER JOIN visa_category AS vc ON vr.visa_category = vc.id WHERE vr.from_country = $idFrom AND vr.to_country = $idTo"
      .query[VisaRequirementsDTO]

}