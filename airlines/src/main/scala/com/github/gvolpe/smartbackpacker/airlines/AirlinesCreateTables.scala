package com.github.gvolpe.smartbackpacker.airlines

import cats.Monad
import cats.effect.Async
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

object AirlinesCreateTables {
  def apply[F[_] : Async]: AirlinesCreateTables[F] =
    new AirlinesCreateTables[F](
      Transactor.fromDriverManager[F](
        "org.postgresql.Driver", "jdbc:postgresql:sb", "postgres", "postgres"
      )
    )
}

class AirlinesCreateTables[F[_] : Monad](xa: Transactor[F]) {

  def run: F[Unit] = createTables.transact(xa)

  private def createTables: ConnectionIO[Unit] =
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
