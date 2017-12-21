package com.github.gvolpe.smartbackpacker.scraper

import cats.Applicative
import cats.effect.IO
import cats.instances.list._
import com.github.gvolpe.smartbackpacker.common.IOApp
import com.github.gvolpe.smartbackpacker.model._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{Instant, Seconds}

object ScraperJob extends IOApp {

  private val ctx = new ScraperModule[IO]

  val visaIndexProgram: IO[Unit] =
    for {
      _       <- IO { println("Starting visa index scraping job") }
      ranking <- ctx.visaRestrictionsIndexParser.parse
      _       <- IO { println("Starting visa index inserting data job") }
      _       <- ctx.visaRestrictionsInsertData.run(ranking)
      _       <- IO { println("Visa index scraping job done") }
    } yield ()

  val countriesProgram: IO[Unit] =
    for {
      _ <- IO { println("Starting countries inserting data job") }
      _ <- ctx.countryInsertData.run
      _ <- IO { println("Countries inserting data job DONE") }
    } yield ()

  val visaCategoriesProgram: IO[Unit] =
    for {
      _ <- IO { println("Starting visa categories inserting data job") }
      _ <- ctx.visaCategoryInsertData.run
      _ <- IO { println("Visa categories inserting data job DONE") }
    } yield ()

  def visaRequirementsProgramFor(from: CountryCode): IO[Unit] =
    for {
      _ <- IO { println(s"${from.value} >> Starting visa requirements job") }
      _ <- ctx.visaRequirementsInsertData.run(from)
      _ <- IO { println(s"${from.value} >> Visa requirements job DONE") }
    } yield ()

  val visaRequirementsProgram: IO[Unit] = {
    ctx.scraperConfig.countriesCode() flatMap { codes =>
      Applicative[IO].traverse(codes)(c => visaRequirementsProgramFor(c)).map(_ => ())
    }
  }

  def healthInfoProgramFor(cc: CountryCode): IO[Unit] =
    for {
      _ <- IO { println("Starting health info inserting data job") }
      _ <- ctx.healthInfoInsertData.run(cc)
      _ <- IO { println("Health info inserting data job DONE") }
    } yield ()

  val healthInfoProgram: IO[Unit] = {
    ctx.scraperConfig.countriesCode() flatMap { codes =>
      Applicative[IO].traverse(codes)(c => healthInfoProgramFor(c)).map(_ => ())
    }
  }

  case object MissingArgument extends Exception("There should be only one argument with one of the following values: `loadCountries`, `loadVisaCategories`, `visaRequirements` or `healthInfo`")

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
      _ <- if (ctx.devDbUrl.nonEmpty) IO { println(s"DEV DB connection established: ${ctx.devDbUrl}") }
           else IO { println(s"DB connection established: ${ctx.dbUrl}") }
      _ <- IO { println(s"Starting job at ${start.toString(fmt)}") }
      _ <- readArgs(args)
      f = Instant.now()
      _ <- IO { println(s"Finished job at ${f.toString(fmt)}. Duration ${Seconds.secondsBetween(start, f).getSeconds} seconds") }
    } yield ()
  }

}
