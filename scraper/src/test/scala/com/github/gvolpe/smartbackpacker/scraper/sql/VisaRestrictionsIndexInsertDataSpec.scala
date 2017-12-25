package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model.{Count, CountryCode, Ranking, Sharing, VisaRestrictionsIndex}
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.scalatest.FunSuite

class VisaRestrictionsIndexInsertDataSpec extends FunSuite with VisaRestrictionsIndexFixture {

  test("create table visa_restrictions_index and insert data") {
    IOAssertion {
      for {
        xa <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
        _  <- createVisaIndexTable(xa)
        _  <- new VisaRestrictionsIndexInsertData[IO](xa).run(countries)
      } yield ()
    }
  }

}

trait VisaRestrictionsIndexFixture {

  val countries = List(
    (new CountryCode("AR"), VisaRestrictionsIndex(new Ranking(1), new Count(176), new Sharing(1))),
    (new CountryCode("DE"), VisaRestrictionsIndex(new Ranking(2), new Count(175), new Sharing(1))),
    (new CountryCode("JP"), VisaRestrictionsIndex(new Ranking(3), new Count(173), new Sharing(2)))
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