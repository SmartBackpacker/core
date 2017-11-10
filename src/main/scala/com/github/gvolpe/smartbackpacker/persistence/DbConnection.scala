package com.github.gvolpe.smartbackpacker.persistence

import cats.effect.Async
import cats.instances.list._
import com.github.gvolpe.smartbackpacker.model.{Airline, BaggageAllowance, BaggagePolicy, BaggageSize, CabinBag, SmallBag}
import doobie.implicits._
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.update.Update

object DbConnection {
  def apply[F[_] : Async]: DbConnection[F] = new DbConnection[F]()
}

class DbConnection[F[_] : Async] {

  val xa: Aux[F, Unit] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver", "jdbc:postgresql:sb", "postgres", "postgres"
  )

  def insertAirline(name: String): ConnectionIO[Int] = {
    sql"INSERT INTO airline (name) VALUES ($name)"
      .update.withUniqueGeneratedKeys("airline_id")
  }

  def insertBaggagePolicy(airlineId: Int,
                          baggagePolicy: BaggagePolicy): ConnectionIO[Int] = {
    sql"INSERT INTO baggage_policy (airline_id, extra, website) VALUES ($airlineId, ${baggagePolicy.extra}, ${baggagePolicy.website})"
      .update.withUniqueGeneratedKeys("policy_id")
  }

  type BaggageAllowanceDTO = (Int, String, Option[Int], Int, Int, Int)

  implicit class BaggageAllowanceOps(baggageAllowance: List[BaggageAllowance]) {
    def toDTO(policyId: Int): List[BaggageAllowanceDTO] = {
      baggageAllowance.map { ba =>
        (policyId, ba.baggageType.toString, ba.kgs, ba.size.height, ba.size.width, ba.size.depth)
      }
    }
  }

  def insertManyBaggageAllowance(policyId: Int,
                                 baggageAllowance: List[BaggageAllowance]): ConnectionIO[Int] = {
    val sql = "INSERT INTO baggage_allowance (policy_id, baggage_type, kgs, height, width, depth) VALUES (?, ?, ?, ?, ?, ?)"
    Update[BaggageAllowanceDTO](sql).updateMany(baggageAllowance.toDTO(policyId))
  }

  val airline: Airline = AirlinesInMemory.a

  val program: ConnectionIO[Unit] =
    for {
      airlineId <- insertAirline(airline.name)
      policyId  <- insertBaggagePolicy(airlineId, airline.baggagePolicy)
      _         <- insertManyBaggageAllowance(policyId, airline.baggagePolicy.allowance)
    } yield ()

  def run: F[Unit] = program.transact(xa)

}

object AirlinesInMemory {

  val a = Airline("Aer Lingus", BaggagePolicy(
    allowance = List(
      BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 24)),
      BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
    ),
    extra = None,
    website = Some("https://www.aerlingus.com/travel-information/baggage-information/cabin-baggage/"))
  )

}