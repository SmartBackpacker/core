package com.github.gvolpe.smartbackpacker.airlines

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AllowanceFile}
import com.github.gvolpe.smartbackpacker.airlines.sql.{AirlinesCreateTables, AirlinesInsertData}

// See: https://wikitravel.org/en/Discount_airlines_in_Europe
object AirlinesJob extends IOApp {

  case object AirlineFileNotFound extends Exception("Airlines file not defined")
  case object AllowanceFileNotFound extends Exception("Allowance file not defined")

  def readArgs(args: List[String]): IO[(AirlineFile, AllowanceFile)] = {
    for {
      x <- args.headOption.fold(IO.raiseError[String](AirlineFileNotFound))(a => IO(a))
      y <- args.lastOption.fold(IO.raiseError[String](AllowanceFileNotFound))(a => IO(a))
    } yield (new AirlineFile(x), new AllowanceFile(y))
  }

  def program(airlineFile: AirlineFile, allowanceFile: AllowanceFile): IO[Unit] =
    for {
      _       <- IO { println("Starting job") }
      _       <- AirlinesCreateTables[IO].run
      _       <- AirlinesInsertData[IO](airlineFile, allowanceFile).run
      _       <- IO { println("Job finished successfully") }
    } yield ()

  override def start(args: List[String]): IO[Unit] =
    for {
      files   <- readArgs(args)
      (x, y)  = files
      _       <- program(x, y)
    } yield ()

}
