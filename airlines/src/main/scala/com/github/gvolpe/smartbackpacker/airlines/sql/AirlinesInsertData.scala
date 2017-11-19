package com.github.gvolpe.smartbackpacker.airlines.sql

import cats.Applicative
import cats.effect.{Async, Effect}
import cats.instances.list._
import cats.instances.vector._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import com.github.gvolpe.smartbackpacker.model.{Airline, BaggageAllowance, BaggagePolicy}
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update

object AirlinesInsertData {
  def apply[F[_] : Effect](airlineFile: AirlineFile, allowanceFile: AllowanceFile): AirlinesInsertData[F] =
    new AirlinesInsertData[F](
      Transactor.fromDriverManager[F](
        "org.postgresql.Driver", "jdbc:postgresql:sb", "postgres", "postgres"
      ),
      AirlinesFileParser[F](airlineFile, allowanceFile)
    )
}

class AirlinesInsertData[F[_] : Async](xa: Transactor[F], airlinesParser: AirlinesFileParser[F]) {

  private def insertAirline(name: String): ConnectionIO[Int] = {
    sql"INSERT INTO airline (name) VALUES ($name)"
      .update.withUniqueGeneratedKeys("airline_id")
  }

  private def insertBaggagePolicy(airlineId: Int,
                                  baggagePolicy: BaggagePolicy): ConnectionIO[Int] = {
    sql"INSERT INTO baggage_policy (airline_id, extra, website) VALUES ($airlineId, ${baggagePolicy.extra}, ${baggagePolicy.website})"
      .update.withUniqueGeneratedKeys("policy_id")
  }

  private def insertManyBaggageAllowance(policyId: Int,
                                         baggageAllowance: List[BaggageAllowance]): ConnectionIO[Int] = {
    val sql = "INSERT INTO baggage_allowance (policy_id, baggage_type, kgs, height, width, depth) VALUES (?, ?, ?, ?, ?, ?)"
    Update[CreateBaggageAllowanceDTO](sql).updateMany(baggageAllowance.toDTO(policyId))
  }

  private def program(airline: Airline): ConnectionIO[Unit] =
    for {
      airlineId <- insertAirline(airline.name.value)
      policyId  <- insertBaggagePolicy(airlineId, airline.baggagePolicy)
      _         <- insertManyBaggageAllowance(policyId, airline.baggagePolicy.allowance)
    } yield ()

  def run: F[Unit] = {
    airlinesParser.airlines.runLogSync.flatMap { a =>
      Applicative[F].traverse(a)(a => program(a).transact(xa)).map(_ => ())
    }
  }

}