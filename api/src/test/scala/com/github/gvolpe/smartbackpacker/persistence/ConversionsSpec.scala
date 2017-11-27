package com.github.gvolpe.smartbackpacker.persistence

import com.github.gvolpe.smartbackpacker.model._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks
import shapeless._

class ConversionsSpec extends FunSuite with ConversionsArbitraries with PropertyChecks {

  forAll { (ba: BaggageAllowance) =>
    test(s"convert an AllowanceDto to $ba") {
      val dto: BaggageAllowanceDTO = ba.baggageType.toString :: ba.kgs :: ba.size.height :: ba.size.width :: ba.size.depth :: HNil
      assert(dto.toBaggageAllowance == ba)
    }
  }

  forAll { (ba: BaggageAllowance, dto: AirlineDTO) =>
    test(s"convert a $dto to Airline") {
      val allowanceDto: BaggageAllowanceDTO = ba.baggageType.toString :: ba.kgs :: ba.size.height :: ba.size.width :: ba.size.depth :: HNil

      val expected: Airline = Airline(
        name = new AirlineName(dto(1)),
        baggagePolicy = BaggagePolicy(
          allowance = List(ba),
          extra = dto(3),
          website = dto(4)
        )
      )

      assert(dto.toAirline(List(allowanceDto)) == expected)
    }
  }

  forAll { (dto: RestrictionsIndexDTO) =>
    test(s"convert a $dto to VisaRestrictionsIndex") {
      val expected = VisaRestrictionsIndex(rank = dto(0), count = dto(1), sharing = dto(2))
      assert(dto.toVisaRestrictionsIndex == expected)
    }
  }

  forAll { (index: VisaRestrictionsIndex) =>
    test(s"convert a RestrictionsIndexDTO to $index") {
      val dto = index.rank :: index.count :: index.sharing :: HNil
      assert(dto.toVisaRestrictionsIndex == index)
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
    } yield id :: name :: id :: extra :: web :: HNil
  }

  implicit val restrictionsIndexDTO: Arbitrary[RestrictionsIndexDTO] = Arbitrary[RestrictionsIndexDTO] {
    for {
      r <- Gen.posNum[Int]
      c <- Gen.posNum[Int]
      s <- Gen.posNum[Int]
    } yield r :: c :: s :: HNil
  }

  implicit val visaRestrictionsIndex: Arbitrary[VisaRestrictionsIndex] = Arbitrary[VisaRestrictionsIndex] {
    for {
      r <- Gen.posNum[Int]
      c <- Gen.posNum[Int]
      s <- Gen.posNum[Int]
    } yield VisaRestrictionsIndex(r, c, s)
  }

}
