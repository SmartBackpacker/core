package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.common.instances.log._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.scraper.config.ScraperConfiguration
import com.github.gvolpe.smartbackpacker.scraper.parser.AbstractVisaRequirementsParser
import doobie.free.connection.ConnectionIO
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.FunSuite

import scala.io.Source

class VisaRequirementsInsertDataSpec extends FunSuite with VisaRequirementsInsertDataFixture {

  private val scraperConfig = new ScraperConfiguration[IO]

  private val parser = new AbstractVisaRequirementsParser[IO](scraperConfig) {
    override def htmlDocument(from: CountryCode): IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"wikiPageTest-${from.value}.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }
  }

  // H2 does not recognize the complex SQL syntax used in this query
  ignore("create visa requirements tables and insert data") {
    IOAssertion {
      for {
        xa <- H2Transactor[IO]("jdbc:h2:mem:vr_sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
        _  <- createHealthTables(xa)
        _  <- new CountryInsertData[IO](scraperConfig, xa).run
        _  <- new VisaCategoryInsertData[IO](xa).run
        _  <- new VisaRequirementsInsertData[IO](xa, parser).run("AR".as[CountryCode])
      } yield ()
    }
  }

}

trait VisaRequirementsInsertDataFixture {

  val countries = List(
    (new CountryCode("AR"), VisaRestrictionsIndex(new Ranking(1), new Count(176), new Sharing(1))),
    (new CountryCode("DE"), VisaRestrictionsIndex(new Ranking(2), new Count(175), new Sharing(1))),
    (new CountryCode("JP"), VisaRestrictionsIndex(new Ranking(3), new Count(173), new Sharing(2)))
  )

  private val createTableCountries: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE countries (
           id SERIAL PRIMARY KEY,
           code VARCHAR (2) NOT NULL UNIQUE,
           name VARCHAR (100) NOT NULL UNIQUE
         )
       """.update.run
  }

  private val createTableVisaCategory: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE visa_category (
           id SERIAL PRIMARY KEY,
           name VARCHAR (200)
         )
       """.update.run
  }

  private val createTableVisaRequirements: ConnectionIO[Int] = {
    sql"""
         CREATE TABLE visa_requirements (
           from_country INT NOT NULL,
           to_country INT NOT NULL,
           visa_category INT REFERENCES visa_category (id),
           description VARCHAR (500)
         )
       """.update.run
  }

  private val createVisaRequirementsFK1: ConnectionIO[Int] = {
    sql"ALTER TABLE visa_requirements ADD CONSTRAINT fk_from FOREIGN KEY (from_country) REFERENCES countries (id)"
      .update.run
  }

  private val createVisaRequirementsFK2: ConnectionIO[Int] = {
    sql"ALTER TABLE visa_requirements ADD CONSTRAINT fk_to FOREIGN KEY (to_country) REFERENCES countries (id)"
      .update.run
  }

  private val createVisaRequirementsPK: ConnectionIO[Int] = {
    sql"ALTER TABLE visa_requirements ADD PRIMARY KEY (from_country, to_country)"
      .update.run
  }

  def createHealthTables(xa: Transactor[IO]): IO[Unit] =
    for {
      _ <- createTableCountries.transact(xa)
      _ <- createTableVisaCategory.transact(xa)
      _ <- createTableVisaRequirements.transact(xa)
      _ <- createVisaRequirementsFK1.transact(xa)
      _ <- createVisaRequirementsFK2.transact(xa)
      _ <- createVisaRequirementsPK.transact(xa)
    } yield ()

}