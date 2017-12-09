package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model.{CountryCode, VisaRestrictionsIndex}
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.scalatest.FunSuite

class VisaRestrictionsIndexInsertDataSpec extends FunSuite with VisaRestrictionsIndexFixture {

  test("create table visa_restrictions_index and insert data") {
    IOAssertion {
      for {
        xa <- H2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
        _  <- createVisaIndexTable(xa)
        _  <- new VisaRestrictionsIndexInsertData[IO](xa).run(countries)
      } yield ()
    }
  }

}

trait VisaRestrictionsIndexFixture {

  val countries = List(
    (new CountryCode("AR"), VisaRestrictionsIndex(1,176,1)),
    (new CountryCode("DE"), VisaRestrictionsIndex(2,175,1)),
    (new CountryCode("JP"), VisaRestrictionsIndex(3,173,2))
  )

  private def createVisaIndexTableStatement: Update0 =
    sql"""
         CREATE TABLE visa_restrictions_index (
           country_code VARCHAR (2) PRIMARY KEY,
           rank SMALLINT NOT NULL,
           acc SMALLINT NOT NULL,
           sharing SMALLINT DEFAULT 0
         )
       """.update

  def createVisaIndexTable(xa: Transactor[IO]): IO[Int] =
    createVisaIndexTableStatement.run.transact(xa)
}