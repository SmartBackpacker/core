package com.github.gvolpe.smartbackpacker.airlines

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AllowanceFile}
import com.github.gvolpe.smartbackpacker.airlines.sql.{AirlinesCreateTables, AirlinesInsertData}

// See: https://wikitravel.org/en/Discount_airlines_in_Europe
object AirlinesJob extends IOApp {

  case object AirlineFileNotFound extends Exception("Environment variable not defined: SB_AIRLINES_FILE.")
  case object AllowanceFileNotFound extends Exception("Environment variable not defined: SB_AIRLINES_FILE.")

  val readSystemEnv: IO[(AirlineFile, AllowanceFile)] = {
    for {
      _ <- IO { println("Reading environment variables") }
      x <- sys.env.get("SB_AIRLINES_FILE").fold(IO.raiseError[String](AirlineFileNotFound))(a => IO(a))
      y <- sys.env.get("SB_ALLOWANCE_FILE").fold(IO.raiseError[String](AllowanceFileNotFound))(a => IO(a))
    } yield (new AirlineFile(x), new AllowanceFile(y))
  }

  def program(airlineFile: AirlineFile, allowanceFile: AllowanceFile): IO[Unit] =
    for {
      _       <- IO { println("Starting job") }
//      _       <- AirlinesCreateTables[IO].run
      _       <- AirlinesInsertData[IO](airlineFile, allowanceFile).run
      _       <- IO { println("Job finished successfully") }
    } yield ()

  override def start(args: List[String]): IO[Unit] =
    for {
      files   <- readSystemEnv
      (x, y)  = files
      _       <- program(x, y)
    } yield ()

}
