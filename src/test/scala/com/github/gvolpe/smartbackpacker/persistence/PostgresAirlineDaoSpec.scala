package com.github.gvolpe.smartbackpacker.persistence

import cats.effect.IO
import org.scalatest.{FlatSpecLike, Matchers}

class PostgresAirlineDaoSpec extends FlatSpecLike with Matchers {

  behavior of "PostgresAirlineDao"

  // TODO: this DAO should have the Transactor as a constructor parameter so we can pass an in memory transactor like H2 for test
  val airlineDao = new PostgresAirlineDao[IO] // (H2Transactor...)

  it should "" in {

  }

}
