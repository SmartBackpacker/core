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
import com.smartbackpackerapp.repository.algebra.VisaRestrictionsIndexRepository
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import doobie.util.query.Query0
import doobie.util.transactor.Transactor

class PostgresVisaRestrictionsIndexRepository[F[_]](xa: Transactor[F])
                                                   (implicit F: MonadError[F, Throwable]) extends VisaRestrictionsIndexRepository[F] {

  override def findRestrictionsIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]] = {
    val index = VisaRestrictionsIndexStatement.findIndex(countryCode).unique

    index.map(_.toVisaRestrictionsIndex).map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[VisaRestrictionsIndex].pure[F]
    }
  }

}

object VisaRestrictionsIndexStatement {

  def findIndex(countryCode: CountryCode): Query0[RestrictionsIndexDTO] =
    sql"SELECT rank, acc, sharing FROM visa_restrictions_index WHERE country_code = ${countryCode.value}"
      .query[RestrictionsIndexDTO]

}