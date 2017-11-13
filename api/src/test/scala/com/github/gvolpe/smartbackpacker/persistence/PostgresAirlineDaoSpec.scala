package com.github.gvolpe.smartbackpacker.persistence

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.model.AirlineName
import doobie.free.connection.ConnectionIO
import doobie.h2._
import doobie.implicits._
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class PostgresAirlineDaoSpec extends FlatSpecLike with Matchers with PostgreSQLSetup with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    super.beforeAll()

    val program = for {
      xa  <- H2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
      _   <- createTables.transact(xa)
    } yield ()

    program.unsafeRunSync()
  }

  it should "not find the airline" in {

    val program = for {
      xa  <- H2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
      dao = new PostgresAirlineDao[IO](xa)
      res <- dao.findAirline(new AirlineName("Ryan Air"))
    } yield {
      res should be (None)
    }

    program.unsafeRunSync()
  }

}

trait PostgreSQLSetup {

  def createTables: ConnectionIO[Unit] =
    for {
      _ <- createAirlineTable
      _ <- createBaggagePolicyTable
      _ <- createBaggageAllowanceTable
    } yield ()

  // TOOD: AirlinesInsertData[F] is not visible from here now
  //def insertData = ???

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