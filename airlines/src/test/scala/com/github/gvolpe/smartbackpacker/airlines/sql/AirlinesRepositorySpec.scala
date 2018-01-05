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

package com.github.gvolpe.smartbackpacker.airlines.sql

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.h2.H2Transactor
import org.scalatest.{FlatSpecLike, Matchers}

class AirlinesRepositorySpec extends AirlinesDataFixture with FlatSpecLike with Matchers {

  it should "Create tables and insert data from files" in IOAssertion  {
    for {
      xa <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
      _  <- createTables.transact(xa)
      _  <- new AirlinesInsertData[IO](xa, parser).run
    } yield ()
  }

}

trait AirlinesDataFixture {

  val parser: AirlinesFileParser[IO] = AirlinesFileParser[IO](
    new AirlineFile(getClass.getResource("/airlines-file-sample").getPath),
    new AllowanceFile(getClass.getResource("/allowance-file-sample").getPath)
  )

  def createTables: ConnectionIO[Unit] =
    for {
      _ <- createAirlineTable
      _ <- createBaggagePolicyTable
      _ <- createBaggageAllowanceTable
    } yield ()

  private val createAirlineTable: ConnectionIO[Int] =
    sql"""
      CREATE TABLE airline (
        airline_id SERIAL PRIMARY KEY,
        name VARCHAR (100) NOT NULL UNIQUE
      )
      """.update.run

  private val createBaggagePolicyTable: ConnectionIO[Int] =
    sql"""
      CREATE TABLE baggage_policy (
        policy_id SERIAL PRIMARY KEY,
        airline_id INT REFERENCES airline (airline_id),
        extra VARCHAR (500),
        website VARCHAR (500)
      )
      """.update.run

  private val createBaggageAllowanceTable: ConnectionIO[Int] =
    sql"""
      CREATE TABLE baggage_allowance (
        allowance_id SERIAL PRIMARY KEY,
        policy_id INT REFERENCES baggage_policy (policy_id),
        baggage_type VARCHAR (25) NOT NULL,
        kgs SMALLINT,
        height SMALLINT NOT NULL,
        width SMALLINT NOT NULL,
        depth SMALLINT NOT NULL
      )
      """.update.run

}
