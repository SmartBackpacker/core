package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.scraper.config.ScraperConfiguration
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.scalatest.FunSuite

class CountryInsertDataSpec extends FunSuite with CountryFixture {

  private val scraperConfig = new ScraperConfiguration[IO]

  test("create table country and insert data") {
    IOAssertion {
      for {
        xa <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
        _  <- createCountryTable(xa)
        _  <- new CountryInsertData[IO](scraperConfig, xa).run
      } yield ()
    }
  }

}

trait CountryFixture {

  private def createCountryTableStatement: Update0 =
    sql"""
         CREATE TABLE countries (
           id SERIAL PRIMARY KEY,
           code VARCHAR (2) NOT NULL UNIQUE,
           name VARCHAR (100) NOT NULL UNIQUE
         )
       """.update

  def createCountryTable(xa: Transactor[IO]): IO[Int] =
    createCountryTableStatement.run.transact(xa)

}
