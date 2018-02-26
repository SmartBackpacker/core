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

package com.smartbackpackerapp.airlines

import cats.effect.{Effect, IO}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.smartbackpackerapp.airlines.parser.{AirlineFile, AllowanceFile}
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}

object AirlinesApp extends AirlinesJob[IO]

// See: https://wikitravel.org/en/Discount_airlines_in_Europe
class AirlinesJob[F[_]](implicit F: Effect[F]) extends StreamApp[F] {

  private val ctx = new AirlinesModule[F]

  case object MissingArgument extends Exception("There should be 2 arguments in the following order: `Airline file path` and `Allowance file path`.")

  private def putStrLn(value: String): Stream[F, Unit] = Stream.eval(F.delay(println(value)))

  def readArgs(args: List[String]): F[(AirlineFile, AllowanceFile)] = {
    val ifEmpty = F.raiseError[String](MissingArgument)
    for {
      x <- args.headOption.fold(ifEmpty)(F.delay(_))
      y <- args.lastOption.fold(ifEmpty)(F.delay(_))
    } yield (AirlineFile(x), AllowanceFile(y))
  }

  def program(airlineFile: AirlineFile,
              allowanceFile: AllowanceFile): Stream[F, Unit] =
    for {
      _ <- if (ctx.devDbUrl.nonEmpty) putStrLn(s"DEV DB connection established: ${ctx.devDbUrl}")
           else putStrLn(s"DB connection established: ${ctx.dbUrl}")
      _ <- putStrLn("Starting job")
      _ <- ctx.airlinesInsertData(airlineFile, allowanceFile).run
      _ <- putStrLn("Job finished successfully")
    } yield ()

  def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      files    <- Stream.eval(readArgs(args))
      (x, y)   = files
      exitCode <- program(x, y).drain
    } yield exitCode

}
