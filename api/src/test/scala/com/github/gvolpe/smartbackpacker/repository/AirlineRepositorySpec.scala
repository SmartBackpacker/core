package com.github.gvolpe.smartbackpacker.repository

import cats.Applicative
import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model._
import doobie.free.connection.ConnectionIO
import doobie.h2._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class AirlineRepositorySpec extends AirlineSQLSetup with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override val h2Transactor: IO[H2Transactor[IO]] =
    H2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")

  override def beforeAll(): Unit = {
    super.beforeAll()
    setup.unsafeRunSync()
  }

  it should "find and NOT find the airline" in IOAssertion {
    for {
      xa    <- h2Transactor
      repo  = new PostgresAirlineRepository[IO](xa)
      rs1   <- repo.findAirline(new AirlineName("Aer Lingus"))
      rs2   <- repo.findAirline(new AirlineName("Ryan Air"))
    } yield {
      rs1 should be (Some(airlines.head))
      rs2 should be (None)
    }
  }

}

trait AirlineSQLSetup {

  import cats.instances.list._

  def h2Transactor: IO[H2Transactor[IO]]

  lazy val setup: IO[Unit] =
    for {
      xa  <- h2Transactor
      _   <- createTables.transact(xa)
      _   <- insertData(xa)
    } yield ()

  protected val airlines: List[Airline] = List(
    Airline("Aer Lingus".as[AirlineName], BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 24)),
        BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
      ),
      extra = None,
      website = Some("https://www.aerlingus.com/travel-information/baggage-information/cabin-baggage/"))
    )
  )

  def createTables: ConnectionIO[Unit] =
    for {
      _ <- createAirlineTable
      _ <- createBaggagePolicyTable
      _ <- createBaggageAllowanceTable
    } yield ()

  def insertData(xa: Transactor[IO]): IO[List[Unit]] = {
    Applicative[IO].traverse(airlines)(a => insertDataProgram(a).transact(xa))
  }

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

  type CreateAllowanceDTO = (Int, String, Option[Int], Int, Int, Int)

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
    Update[CreateAllowanceDTO](sql).updateMany(baggageAllowance.toDTO(policyId))
  }

  private def insertDataProgram(airline: Airline): ConnectionIO[Unit] =
    for {
      airlineId <- insertAirline(airline.name.value)
      policyId  <- insertBaggagePolicy(airlineId, airline.baggagePolicy)
      _         <- insertManyBaggageAllowance(policyId, airline.baggagePolicy.allowance)
    } yield ()

  implicit class BaggageAllowanceOps(baggageAllowance: List[BaggageAllowance]) {
    def toDTO(policyId: Int): List[CreateAllowanceDTO] = {
      baggageAllowance.map { ba =>
        (policyId, ba.baggageType.toString, ba.kgs, ba.size.height, ba.size.width, ba.size.depth)
      }
    }
  }

}