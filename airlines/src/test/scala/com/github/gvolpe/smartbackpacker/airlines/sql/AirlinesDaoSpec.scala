package com.github.gvolpe.smartbackpacker.airlines.sql

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import doobie.h2.H2Transactor
import org.scalatest.{FlatSpecLike, Matchers}

class AirlinesDaoSpec extends FlatSpecLike with Matchers {

  private val parser = AirlinesFileParser[IO](
    new AirlineFile(getClass.getResource("/airlines-file-sample").getPath),
    new AllowanceFile(getClass.getResource("/allowance-file-sample").getPath)
  )

  it should "Create tables and insert data from files" in {
    val program = for {
      xa <- H2Transactor[IO]("jdbc:h2:mem:sb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")
      _  <- new AirlinesCreateTables[IO](xa).run
      _  <- new AirlinesInsertData[IO](xa, parser).run
    } yield ()

    program.unsafeRunSync()
  }

}
