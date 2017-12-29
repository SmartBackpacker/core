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

package com.github.gvolpe.smartbackpacker.airlines

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AllowanceFile}
import com.github.gvolpe.smartbackpacker.common.IOApp

// See: https://wikitravel.org/en/Discount_airlines_in_Europe
object AirlinesJob extends IOApp {

  private val ctx = new AirlinesModule[IO]

  case object MissingArgument extends Exception("There should be 2 arguments in the following order: `Airline file path` and `Allowance file path`.")

  def readArgs(args: List[String]): IO[(AirlineFile, AllowanceFile)] = {
    val ifEmpty = IO.raiseError[String](MissingArgument)
    for {
      x <- args.headOption.fold(ifEmpty)(IO(_))
      y <- args.lastOption.fold(ifEmpty)(IO(_))
    } yield (new AirlineFile(x), new AllowanceFile(y))
  }

  def program(airlineFile: AirlineFile,
              allowanceFile: AllowanceFile): IO[Unit] =
    for {
      _       <- if (ctx.devDbUrl.nonEmpty) putStrLn(s"DEV DB connection established: ${ctx.devDbUrl}")
                 else putStrLn(s"DB connection established: ${ctx.dbUrl}")
      _       <- putStrLn("Starting job")
      _       <- ctx.airlinesInsertData(airlineFile, allowanceFile).run
      _       <- putStrLn("Job finished successfully")
    } yield ()

  override def start(args: List[String]): IO[Unit] =
    for {
      files  <- readArgs(args)
      (x, y) = files
      _      <- program(x, y)
    } yield ()

}
