package com.github.gvolpe.smartbackpacker.scraper
import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOApp
import com.github.gvolpe.smartbackpacker.scraper.parser.VisaRestrictionsIndexParser
import com.github.gvolpe.smartbackpacker.scraper.sql.VisaRestrictionsIndexInsertData
import doobie.util.transactor.Transactor

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

  private val indexParser = new VisaRestrictionsIndexParser[IO]
  private val insertData  = new VisaRestrictionsIndexInsertData[IO](xa)

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
      _ <- visaIndexProgram
    } yield ()

}
