/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbackpackerapp.scraper

import cats.effect.IO
import cats.instances.list._
import cats.syntax.apply._
import cats.syntax.traverse._
import com.smartbackpackerapp.common.IOApp
import com.smartbackpackerapp.model._
import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat

object ScraperJob extends IOApp {

  private val ctx = new ScraperModule[IO]

  val visaIndexProgram: IO[Unit] =
    for {
      _       <- putStrLn("Starting visa index scraping job")
      ranking <- ctx.visaRestrictionsIndexParser.parse
      _       <- putStrLn("Starting visa index inserting data job")
      _       <- ctx.visaRestrictionsInsertData.run(ranking)
      _       <- putStrLn("Visa index scraping job done")
    } yield ()

  val updateCountriesProgram: IO[Unit] =
    for {
      _ <- putStrLn("Starting countries updating data job")
      _ <- ctx.countryInsertData.runUpdate
      _ <- putStrLn("Countries updating data job DONE")
    } yield ()

  val countriesProgram: IO[Unit] =
    for {
      _ <- putStrLn("Starting countries inserting data job")
      _ <- ctx.countryInsertData.run
      _ <- putStrLn("Countries inserting data job DONE")
    } yield ()

  val visaCategoriesProgram: IO[Unit] =
    for {
      _ <- putStrLn("Starting visa categories inserting data job")
      _ <- ctx.visaCategoryInsertData.run
      _ <- putStrLn("Visa categories inserting data job DONE")
    } yield ()

  def visaRequirementsProgramFor(from: CountryCode): IO[Unit] =
    for {
      _ <- putStrLn(s"${from.value} >> Starting visa requirements job")
      _ <- ctx.visaRequirementsInsertData.run(from)
      _ <- putStrLn(s"${from.value} >> Visa requirements job DONE")
    } yield ()

  val visaRequirementsProgram: IO[Unit] = {
    ctx.scraperConfig.countriesCode() flatMap { codes =>
      codes.traverse(c => visaRequirementsProgramFor(c)) *> IO.unit
    }
  }

  def healthInfoProgramFor(cc: CountryCode): IO[Unit] =
    for {
      _ <- putStrLn("Starting health info inserting data job")
      _ <- ctx.healthInfoInsertData.run(cc)
      _ <- putStrLn("Health info inserting data job DONE")
    } yield ()

  val healthInfoProgram: IO[Unit] = {
    ctx.scraperConfig.countriesCode() flatMap { codes =>
      codes.traverse(c => healthInfoProgramFor(c)) *> IO.unit
    }
  }

  case object MissingArgument extends Exception("There should be only one argument with one of the following values: `loadCountries`, `updateCountries`, `loadVisaCategories`, `visaRequirements`, `visaRanking` or `healthInfo`")

  def readArgs(args: List[String]): IO[Unit] = {
    val ifEmpty = IO.raiseError[Unit](MissingArgument)
    args.headOption.fold(ifEmpty) {
      case "loadCountries"      => countriesProgram
      case "updateCountries"    => updateCountriesProgram
      case "loadVisaCategories" => visaCategoriesProgram
      case "visaRequirements"   => visaRequirementsProgram
      case "visaRanking"        => visaIndexProgram
      case "healthInfo"         => healthInfoProgram
      case _                    => ifEmpty
    }
  }

  override def start(args: List[String]): IO[Unit] = {
    lazy val fmt    = DateTimeFormat.forPattern("H:m:s.S")
    for {
      start <- getTime
      _     <- if (ctx.devDbUrl.nonEmpty) putStrLn(s"DEV DB connection established: ${ctx.devDbUrl}")
               else putStrLn(s"DB connection established: ${ctx.dbUrl}")
      _     <- putStrLn(s"Starting job at ${start.toString(fmt)}")
      _     <- readArgs(args)
      end   <- getTime
      _     <- putStrLn(s"Finished job at ${end.toString(fmt)}. Duration ${Seconds.secondsBetween(start, end).getSeconds} seconds")
    } yield ()
  }

}
