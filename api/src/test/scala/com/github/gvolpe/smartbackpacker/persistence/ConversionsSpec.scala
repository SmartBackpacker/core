package com.github.gvolpe.smartbackpacker.persistence

import com.github.gvolpe.smartbackpacker.model.{Airline, BaggageAllowance, BaggagePolicy, BaggageSize, CabinBag, SmallBag}
import org.scalatest.{FlatSpecLike, Matchers}
import shapeless._

class ConversionsSpec extends FlatSpecLike with Matchers {

  it should "convert an AllowanceDTO to BaggageAllowance" in {
    val dto: AllowanceDTO = "CabinBag" :: Some(10) :: 10 :: 20 :: 30 :: HNil

    val expected: BaggageAllowance = BaggageAllowance(
      baggageType = CabinBag,
      kgs = Some(10),
      size = BaggageSize(10, 20, 30)
    )

    dto.toBaggageAllowance should be (expected)
  }

  it should "convert an AirlineDTO to Airline" in {
    val allowanceDto: AllowanceDTO = "SmallBag" :: Some(10) :: 10 :: 20 :: 30 :: HNil
    val dto: AirlineDTO = 1 :: "Ryan Air" :: 2 :: None :: None :: HNil

    val expectedAllowance: BaggageAllowance = BaggageAllowance(
      baggageType = SmallBag,
      kgs = Some(10),
      size = BaggageSize(10, 20, 30)
    )

    val expected: Airline = Airline(
      name = "Ryan Air",
      baggagePolicy = BaggagePolicy(
        allowance = List(expectedAllowance),
        extra = None,
        website = None
      )
    )

    dto.toAirline(List(allowanceDto)) should be (expected)
  }

}
