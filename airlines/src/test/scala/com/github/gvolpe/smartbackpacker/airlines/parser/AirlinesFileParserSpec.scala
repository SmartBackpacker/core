package com.github.gvolpe.smartbackpacker.airlines.parser

import cats.effect.IO
import org.scalatest.{FlatSpecLike, Matchers}

class AirlinesFileParserSpec extends FlatSpecLike with Matchers {

  private val parser = AirlinesFileParser[IO](
    new AirlineFile(getClass.getResource("/airlines-file-sample").getPath),
    new AllowanceFile(getClass.getResource("/allowance-file-sample").getPath)
  )

  it should "parse the airlines and allowance files" in {
    val result = for {
      a <- parser.airlines
    } yield {
      a.name.value              should not be empty
      a.baggagePolicy.allowance should not be empty
    }
    result.run.unsafeRunSync()
  }

}
