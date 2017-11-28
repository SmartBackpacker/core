package com.github.gvolpe.smartbackpacker.scraper

import cats.Applicative
import cats.effect.IO
import cats.instances.list._
import com.github.gvolpe.smartbackpacker.common.IOApp
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.scraper.parser.{VisaRequirementsParser, VisaRestrictionsIndexParser}
import com.github.gvolpe.smartbackpacker.scraper.sql.{CountryInsertData, VisaCategoryInsertData, VisaRequirementsInsertData, VisaRestrictionsIndexInsertData}
import doobie.util.transactor.Transactor
import org.joda.time.{Instant, Seconds}
import org.joda.time.format.DateTimeFormat

object ScraperJob extends IOApp {

  // Scheduler.oncePerMonth(runJob)

  // TODO
  // - Create Table `visa_requirements`.
  // - Run WikiPageParser once a week for every country and persist the results to PostgreSQL.
  // - Same for VisaRestrictionsIndexParser.
  // - Make sure to have at least the DB backup of a month (4 backups) for resiliency.
  // - Run the parsers in parallel to maximize throughput.
  // - Create CountryDao and VisaRestrictionIndexDao
  // - Modify CountryService and VisaRestrictionIndexService to use the DAOs instead of the Parsers.

  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:sb", "postgres", sys.env.getOrElse("SB_DB_PASSWORD", "")
  )

  private val visaRequirementsParser      = new VisaRequirementsParser[IO]()
  private val visaRequirementsInsertData  = new VisaRequirementsInsertData[IO](xa, visaRequirementsParser)

  private val visaRestrictionsIndexParser = new VisaRestrictionsIndexParser[IO]
  private val visaRestrictionsInsertData  = new VisaRestrictionsIndexInsertData[IO](xa)

  private val countryInsertData           = new CountryInsertData[IO](xa)
  private val visaCategoryInsertData      = new VisaCategoryInsertData[IO](xa)

  // TODO: See how to improve throughput by running different countries in parallel (specially for visa requirements)
  val visaIndexProgram: IO[Unit] =
    for {
      _       <- IO { println("Starting visa index scraping job") }
      ranking <- visaRestrictionsIndexParser.parse
      _       <- IO { println("Starting visa index inserting data job") }
      _       <- visaRestrictionsInsertData.run(ranking)
      _       <- IO { println("Visa index scraping job done") }
    } yield ()

  val countriesProgram: IO[Unit] =
    for {
      _ <- IO { println("Starting countries inserting data job") }
      _ <- countryInsertData.run
      _ <- IO { println("Countries inserting data job DONE") }
    } yield ()

  val visaCategoriesProgram: IO[Unit] =
    for {
      _ <- IO { println("Starting visa categories inserting data job") }
      _ <- visaCategoryInsertData.run
      _ <- IO { println("Visa categories inserting data job DONE") }
    } yield ()

  def visaRequirementsProgramFor(from: CountryCode): IO[Unit] =
    for {
      _ <- IO { println(s"${from.value} >> Starting visa requirements job") }
      _ <- visaRequirementsInsertData.run(from)
      _ <- IO { println(s"${from.value} >> Visa requirements job DONE") }
    } yield ()

  val visaRequirementsProgram: IO[Unit] = {
    val codes  = SBConfiguration.countriesCode()
    Applicative[IO].traverse(codes)(c => visaRequirementsProgramFor(c)).map(_ => ())
  }

  // TODO: Make optional (by main args) to run only index, only visa requirements or both.
  override def start(args: List[String]): IO[Unit] = {
    lazy val fmt    = DateTimeFormat.forPattern("H:m:s.S")
    lazy val start  = Instant.now()
    for {
    //      _ <- countriesProgram
    //      _ <- visaCategoriesProgram
      _ <- IO { println(s"Starting scraping job at ${start.toString(fmt)}")}
      _ <- visaRequirementsProgram
      f = Instant.now()
      _ <- IO { println(s"Finished scraping job at ${f.toString(fmt)}. Duration ${Seconds.secondsBetween(start, f).getSeconds} seconds")}
    } yield ()
  }

}
