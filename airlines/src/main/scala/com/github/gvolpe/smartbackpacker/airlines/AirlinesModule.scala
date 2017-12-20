package com.github.gvolpe.smartbackpacker.airlines

import cats.effect.Async
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import com.github.gvolpe.smartbackpacker.airlines.sql.AirlinesInsertData
import doobie.util.transactor.Transactor

class AirlinesModule[F[_] : Async] {

  val devDbUrl: String  = sys.env.getOrElse("JDBC_DATABASE_URL", "")
  val dbUrl: String     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")

  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  private val xa = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[F](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[F](dbDriver, dbUrl, dbUser, dbPass)
  }

  def airlinesInsertData(airlineFile: AirlineFile,
                         allowanceFile: AllowanceFile): AirlinesInsertData[F] = {
    val parser = AirlinesFileParser[F](airlineFile, allowanceFile)
    new AirlinesInsertData[F](xa, parser)
  }

}
