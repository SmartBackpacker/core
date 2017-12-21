package com.github.gvolpe.smartbackpacker.service

import cats.data.EitherT
import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.AirlineRepository
import org.scalatest.{FlatSpecLike, Matchers}

class AirlineServiceSpec extends FlatSpecLike with Matchers {

  private val testAirline = Airline(
    name = "Ryan Air".as[AirlineName],
    baggagePolicy = BaggagePolicy(
      allowance = List.empty[BaggageAllowance],
      extra = None,
      website = None
    )
  )

  private val repo = new AirlineRepository[IO] {
    override def findAirline(airlineName: AirlineName): IO[Option[Airline]] = IO {
      if (airlineName.value == "Ryan Air") Some(testAirline)
      else None
    }
  }

  private val service  = new AirlineService[IO](repo)

  it should "find the airline" in IOAssertion {
    EitherT(service.baggagePolicy("Ryan Air".as[AirlineName])).map { airline =>
      airline should be (testAirline)
    }.value
  }

  it should "NOT find the airline" in IOAssertion {
    EitherT(service.baggagePolicy("Futur Airways".as[AirlineName])).leftMap { error =>
      error shouldBe a [AirlineNotFound]
    }.value
  }

}
