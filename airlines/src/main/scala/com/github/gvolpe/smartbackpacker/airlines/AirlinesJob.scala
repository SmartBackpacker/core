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
