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

package com.smartbackpackerapp.airlines.sql

import cats.effect.Async
import cats.instances.list._
import com.smartbackpackerapp.airlines.parser.AirlinesFileParser
import com.smartbackpackerapp.common.Log
import com.smartbackpackerapp.model.{Airline, BaggagePolicy}
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.{Update, Update0}
import fs2.Stream

class AirlinesInsertData[F[_] : Async](xa: Transactor[F],
                                       airlinesParser: AirlinesFileParser[F])
                                      (implicit L: Log[F]) {

  import AirlineInsertStatement._

  private def program(airline: Airline): ConnectionIO[Unit] =
    for {
      airlineId <- insertAirline(airline.name.value).withUniqueGeneratedKeys[Int]("airline_id")
      policyId  <- insertBaggagePolicy(airlineId, airline.baggagePolicy).withUniqueGeneratedKeys[Int]("policy_id")
      _         <- insertManyBaggageAllowance(policyId).updateMany(airline.baggagePolicy.allowance.toDTO(policyId))
    } yield ()

  def run: Stream[F, Unit] =
    for {
      a <- airlinesParser.airlines
      _ <- Stream.eval(L.info(s"Persisting: $a"))
      _ <- Stream.eval(program(a).transact(xa))
    } yield ()

}

object AirlineInsertStatement {

  def insertAirline(name: String): Update0 = {
    sql"INSERT INTO airline (name) VALUES ($name)"
      .update
  }

  def insertBaggagePolicy(airlineId: Int,
                          baggagePolicy: BaggagePolicy): Update0 = {
    sql"INSERT INTO baggage_policy (airline_id, extra, website) VALUES ($airlineId, ${baggagePolicy.extra}, ${baggagePolicy.website})"
      .update
  }

  def insertManyBaggageAllowance(policyId: Int): Update[CreateBaggageAllowanceDTO] = {
    val sql = "INSERT INTO baggage_allowance (policy_id, baggage_type, kgs, height, width, depth) VALUES (?, ?, ?, ?, ?, ?)"
    Update[CreateBaggageAllowanceDTO](sql)
  }

}
