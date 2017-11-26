package com.github.gvolpe.smartbackpacker.airlines

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import com.github.gvolpe.smartbackpacker.airlines.sql.{AirlinesCreateTables, AirlinesInsertData}
import com.github.gvolpe.smartbackpacker.common.IOApp
import doobie.util.transactor.Transactor

// See: https://wikitravel.org/en/Discount_airlines_in_Europe
object AirlinesJob extends IOApp {

  case object MissingArgument extends Exception("There should be 3 arguments in the following order: `Create tables` {true | false}, `Airline file path` and `Allowance file path`.")

  def readArgs(args: List[String]): IO[(Boolean, AirlineFile, AllowanceFile)] = {
    val ifEmpty = IO.raiseError[String](MissingArgument)
    for {
      c <- args.headOption.fold(ifEmpty)(IO(_)).map(_.toBoolean)
      x <- args.tail.headOption.fold(ifEmpty)(IO(_))
      y <- args.tail.tail.headOption.fold(ifEmpty)(IO(_))
    } yield (c, new AirlineFile(x), new AllowanceFile(y))
  }

  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:sb", "postgres", sys.env.getOrElse("SB_DB_PASSWORD", "")
  )

  def program(createTables: Boolean,
              airlineFile: AirlineFile,
              allowanceFile: AllowanceFile): IO[Unit] =
    for {
      _       <- IO { println("Starting job") }
      _       <- if (createTables) new AirlinesCreateTables[IO](xa).run else IO.unit
      parser  = AirlinesFileParser[IO](airlineFile, allowanceFile)
      _       <- new AirlinesInsertData[IO](xa, parser).run
      _       <- IO { println("Job finished successfully") }
    } yield ()

  override def start(args: List[String]): IO[Unit] =
    for {
      files     <- readArgs(args)
      (c, x, y) = files
      _         <- program(c, x, y)
    } yield ()

}
