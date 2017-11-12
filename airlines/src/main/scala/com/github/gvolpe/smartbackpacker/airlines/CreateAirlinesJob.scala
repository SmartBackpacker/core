package com.github.gvolpe.smartbackpacker.airlines

import cats.Applicative
import cats.effect.{Async, IO}
import cats.instances.list._
import com.github.gvolpe.smartbackpacker.model.{Airline, BaggageAllowance, BaggagePolicy}
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update

object CreateAirlinesJob {
  new CreateAirlinesJob[IO]().run.unsafeRunSync()
}

class CreateAirlinesJob[F[_] : Async] {

  private val xa: Transactor.Aux[F, Unit] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver", "jdbc:postgresql:sb", "postgres", "postgres"
  )

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
      airlineId <- insertAirline(airline.name)
      policyId  <- insertBaggagePolicy(airlineId, airline.baggagePolicy)
      _         <- insertManyBaggageAllowance(policyId, airline.baggagePolicy.allowance)
    } yield ()

  def run: F[List[Unit]] = {
    Applicative[F].traverse(AirlinesData.airlines)(a => program(a).transact(xa))
  }

}
