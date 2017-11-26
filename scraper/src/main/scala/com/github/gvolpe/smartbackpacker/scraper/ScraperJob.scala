package com.github.gvolpe.smartbackpacker.scraper
import cats.effect.IO

import com.github.gvolpe.smartbackpacker.common.IOApp
import com.github.gvolpe.smartbackpacker.scraper.parser.VisaRestrictionsIndexParser
import com.github.gvolpe.smartbackpacker.scraper.sql.VisaRestrictionsIndexInsertData

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

  private val indexParser = new VisaRestrictionsIndexParser[IO]
  private val insertData  = VisaRestrictionsIndexInsertData[IO]

  val visaIndexProgram: IO[Unit] =
    for {
      _       <- IO { println("Starting visa index scraping job") }
      ranking <- indexParser.parse
      _       <- IO { println("Starting visa index inserting data job") }
      _       <- insertData.run(ranking)
      _       <- IO { println("Visa index scraping job done") }
    } yield ()

  // TODO: Make optional (by main args) to run only index, only visa requirements or both.
  override def start(args: List[String]): IO[Unit] =
    for {
      _       <- visaIndexProgram
    } yield ()

}
