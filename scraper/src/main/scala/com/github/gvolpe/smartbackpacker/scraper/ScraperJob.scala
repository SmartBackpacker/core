package com.github.gvolpe.smartbackpacker.scraper

import cats.Applicative
import cats.effect.IO
import cats.instances.list._
import com.github.gvolpe.smartbackpacker.common.IOApp
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.scraper.config.ScraperConfiguration
import com.github.gvolpe.smartbackpacker.scraper.parser.{HealthInfoParser, VisaRequirementsParser, VisaRestrictionsIndexParser}
import com.github.gvolpe.smartbackpacker.scraper.sql.{CountryInsertData, HealthInfoInsertData, VisaCategoryInsertData, VisaRequirementsInsertData, VisaRestrictionsIndexInsertData}
import doobie.util.transactor.Transactor
import org.joda.time.{Instant, Seconds}
import org.joda.time.format.DateTimeFormat

object ScraperJob extends IOApp {

  private val devDbUrl  = sys.env.getOrElse("JDBC_DATABASE_URL", "")

  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUrl     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  private val xa = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[IO](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[IO](dbDriver, dbUrl, dbUser, dbPass)
  }

  private val visaRequirementsParser      = new VisaRequirementsParser[IO]()
  private val visaRequirementsInsertData  = new VisaRequirementsInsertData[IO](xa, visaRequirementsParser)

  private val visaRestrictionsIndexParser = new VisaRestrictionsIndexParser[IO]
  private val visaRestrictionsInsertData  = new VisaRestrictionsIndexInsertData[IO](xa)

  private val countryInsertData           = new CountryInsertData[IO](xa)
  private val visaCategoryInsertData      = new VisaCategoryInsertData[IO](xa)

  private val healthInfoParser            = new HealthInfoParser[IO]
  private val healthInfoInsertData        = new HealthInfoInsertData[IO](xa, healthInfoParser)

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
    val codes = ScraperConfiguration.countriesCode()
    Applicative[IO].traverse(codes)(c => visaRequirementsProgramFor(c)).map(_ => ())
  }

  def healthInfoProgramFor(cc: CountryCode): IO[Unit] =
    for {
      _ <- IO { println("Starting health info inserting data job") }
      _ <- healthInfoInsertData.run(cc)
      _ <- IO { println("Health info inserting data job DONE") }
    } yield ()

  val healthInfoProgram: IO[Unit] = {
    val codes = ScraperConfiguration.countriesCode()
    Applicative[IO].traverse(codes)(c => healthInfoProgramFor(c)).map(_ => ())
  }

  case object MissingArgument extends Exception("There should be only one argument with one of the following values: `loadCountries`, `loadVisaCategories`, `visaRequirement` or `healthInfo`")

  def readArgs(args: List[String]): IO[Unit] = {
    val ifEmpty = IO.raiseError[Unit](MissingArgument)
    args.headOption.fold(ifEmpty) {
      case "loadCountries"      => countriesProgram
      case "loadVisaCategories" => visaCategoriesProgram
      case "visaRequirements"   => visaRequirementsProgram
      case "visaRanking"        => visaIndexProgram
      case "healthInfo"         => healthInfoProgram
      case _                    => ifEmpty
    }
  }

  override def start(args: List[String]): IO[Unit] = {
    lazy val fmt    = DateTimeFormat.forPattern("H:m:s.S")
    lazy val start  = Instant.now()
    for {
      _ <- if (devDbUrl.nonEmpty) IO { println(s"DEV DB connection established: $devDbUrl") } else IO.unit
      _ <- if (devDbUrl.isEmpty) IO { println(s"DB connection established: $dbUrl") } else IO.unit
      _ <- IO { println(s"Starting job at ${start.toString(fmt)}") }
      _ <- readArgs(args)
      f = Instant.now()
      _ <- IO { println(s"Finished job at ${f.toString(fmt)}. Duration ${Seconds.secondsBetween(start, f).getSeconds} seconds") }
    } yield ()
  }

}
