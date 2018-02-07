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
import com.smartbackpackerapp.repository.algebra.AirlineRepository
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import doobie.util.query.Query0
import doobie.util.transactor.Transactor

class PostgresAirlineRepository[F[_]](xa: Transactor[F])
                                     (implicit F: MonadError[F, Throwable]) extends AirlineRepository[F] {

  override def findAirline(airlineName: AirlineName): F[Option[Airline]] = {
    val program: ConnectionIO[Airline] =
      for {
        a <- AirlineStatement.findAirline(airlineName).unique
        b <- AirlineStatement.baggageAllowance(a.head).to[List]
      } yield a.toAirline(b)

    program.map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[Airline].pure[F]
    }
  }

}

object AirlineStatement {

  def findAirline(airlineName: AirlineName): Query0[AirlineDTO] = {
    sql"SELECT a.airline_id, a.name, b.policy_id, b.extra, b.website FROM airline AS a INNER JOIN baggage_policy AS b ON (a.airline_id = b.airline_id) WHERE a.name=${airlineName.value}"
      .query[AirlineDTO]
  }

  def baggageAllowance(policyId: Int): Query0[BaggageAllowanceDTO] = {
    sql"SELECT baggage_type, kgs, height, width, depth FROM baggage_allowance WHERE policy_id=$policyId"
      .query[BaggageAllowanceDTO]
  }

}