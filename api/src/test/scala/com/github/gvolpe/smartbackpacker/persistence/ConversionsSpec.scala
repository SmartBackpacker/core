package com.github.gvolpe.smartbackpacker.persistence

import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName, BaggageAllowance, BaggagePolicy, BaggageSize, BaggageType, CabinBag, CheckedBag, SmallBag}
import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}
import shapeless._

class ConversionsSpec extends ConversionsArbitraries with FlatSpecLike with Matchers with PropertyChecks {

  forAll { (ba: BaggageAllowance) =>
    it should s"convert an AllowanceDto to $ba" in {
      val dto: AllowanceDTO = ba.baggageType.toString :: ba.kgs :: ba.size.height :: ba.size.width :: ba.size.depth :: HNil
      dto.toBaggageAllowance should be (ba)
    }
  }

  forAll { (ba: BaggageAllowance, dto: AirlineDTO) =>
    it should s"convert a $dto to Airline" in {
      val allowanceDto: AllowanceDTO = ba.baggageType.toString :: ba.kgs :: ba.size.height :: ba.size.width :: ba.size.depth :: HNil

      val expected: Airline = Airline(
        name = new AirlineName(dto(1)),
        baggagePolicy = BaggagePolicy(
          allowance = List(ba),
          extra = dto(3),
          website = dto(4)
        )
      )

      dto.toAirline(List(allowanceDto)) should be (expected)
    }
  }

}

trait ConversionsArbitraries {

  implicit val baggageType: Arbitrary[BaggageType] = Arbitrary[BaggageType] {
    Gen.oneOf(SmallBag, CabinBag, CheckedBag)
  }

  implicit val baggageSize: Arbitrary[BaggageSize] = Arbitrary[BaggageSize] {
    for {
      h <- Gen.posNum[Int]
      w <- Gen.posNum[Int]
      d <- Gen.posNum[Int]
    } yield BaggageSize(h, w, d)
  }

  implicit val baggageAllowance: Arbitrary[BaggageAllowance] = Arbitrary[BaggageAllowance] {
    for {
      bt  <- arbitrary[BaggageType]
      kgs <- Gen.option(Gen.posNum[Int])
      bs  <- arbitrary[BaggageSize]
    } yield BaggageAllowance(bt, kgs, bs)
  }

  implicit val airlineDTO: Arbitrary[AirlineDTO] = Arbitrary[AirlineDTO] {
    for {
      id    <- Gen.posNum[Int]
      name  <- Gen.alphaStr
      extra <- Gen.option(Gen.alphaStr)
      web   <- Gen.option(Gen.alphaStr)
    } yield {
      id :: name :: id :: extra :: web :: HNil
    }
  }

}
