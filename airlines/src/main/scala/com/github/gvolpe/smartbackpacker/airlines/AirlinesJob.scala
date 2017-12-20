package com.github.gvolpe.smartbackpacker.airlines

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import com.github.gvolpe.smartbackpacker.airlines.sql.{AirlinesCreateTables, AirlinesInsertData}
import com.github.gvolpe.smartbackpacker.common.IOApp
import doobie.util.transactor.Transactor

// See: https://wikitravel.org/en/Discount_airlines_in_Europe
object AirlinesJob extends IOApp {

  private val devDbUrl  = sys.env.getOrElse("JDBC_DATABASE_URL", "")
  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUrl     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  private val xa = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[IO](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[IO](dbDriver, dbUrl, dbUser, dbPass)
  }

  case object MissingArgument extends Exception("There should be 3 arguments in the following order: `Create tables` {true | false}, `Airline file path` and `Allowance file path`.")

  def readArgs(args: List[String]): IO[(Boolean, AirlineFile, AllowanceFile)] = {
    val ifEmpty = IO.raiseError[String](MissingArgument)
    for {
      c <- args.headOption.fold(ifEmpty)(IO(_)).map(_.toBoolean)
      x <- args.tail.headOption.fold(ifEmpty)(IO(_))
      y <- args.tail.tail.headOption.fold(ifEmpty)(IO(_))
    } yield (c, new AirlineFile(x), new AllowanceFile(y))
  }

  def program(createTables: Boolean,
              airlineFile: AirlineFile,
              allowanceFile: AllowanceFile): IO[Unit] =
    for {
      _       <- if (devDbUrl.nonEmpty) IO { println(s"DEV DB connection established: $devDbUrl") }
                 else IO { println(s"DB connection established: $dbUrl") }
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
