package com.github.gvolpe.smartbackpacker.persistence

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.model.AirlineName
import doobie.h2._
import org.scalatest.{FlatSpecLike, Matchers}

class PostgresAirlineDaoSpec extends FlatSpecLike with Matchers {

  ignore should "not find the airline" in {

    val program = for {
      xa  <- H2Transactor[IO]("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "")
      //TODO: _   <- CREATE TABLES
      dao = new PostgresAirlineDao[IO](xa)
      res <- dao.findAirline(new AirlineName("Ryan Air"))
    } yield {
      res should be (None)
    }

    program.unsafeRunSync()
  }

}
