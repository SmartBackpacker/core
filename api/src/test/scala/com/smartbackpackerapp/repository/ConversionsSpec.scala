/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbackpackerapp.repository

import com.smartbackpackerapp.model._
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
        name = AirlineName(dto(1)),
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
      val expected = VisaRestrictionsIndex(Ranking(dto(0)), Count(dto(1)), Sharing(dto(2).getOrElse(0)))
      assert(dto.toVisaRestrictionsIndex == expected)
    }
  }

  forAll { (index: VisaRestrictionsIndex) =>
    test(s"convert a RestrictionsIndexDTO to $index") {
      val dto = index.rank.value :: index.count.value :: Option(index.sharing.value) :: HNil
      assert(dto.toVisaRestrictionsIndex == index)
    }
  }

  forAll { (data: VisaRequirementsData) =>
    test(s"convert VisaRequirementsDTO into a $data") {
      val dto   = Option(data.visaCategory.toString) :: Option(data.description) :: HNil
      val from  = 1 :: data.from.code.value :: data.from.name.value :: data.from.currency.value :: HNil
      val to    = 1 :: data.to.code.value :: data.to.name.value :: data.to.currency.value :: HNil
      assert(dto.toVisaRequirementsData(from, to) == data)
    }
  }

  forAll { (v: Vaccine) =>
    test(s"convert VaccineDTO into a $v # ${v.hashCode()}") {
      val dto = v.disease.value :: v.description :: Option(v.diseaseCategories.map(_.toString).mkString(",")) :: HNil
      assert(dto.toVaccine == v)
    }
  }

  forAll { (ha: HealthAlert) =>
    test(s"convert HealthAlertDTO into a $ha # ${ha.hashCode()}") {
      val dto = ha.title :: Option(ha.link.value) :: Option(ha.description) :: HNil
      assert(dto.toHealthAlert == ha)
    }
  }

  forAll { (al: AlertLevel, ha: HealthAlert) =>
    test(s"convert AlertLevelDTO into a $al # ${ha.hashCode()}") {
      val dto = al.toString :: HNil
      assert(dto.toAlertLevel == al)
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
      s <- Gen.option(Gen.posNum[Int])
    } yield r :: c :: s :: HNil
  }

  implicit val visaRestrictionsIndex: Arbitrary[VisaRestrictionsIndex] = Arbitrary[VisaRestrictionsIndex] {
    for {
      r <- Gen.posNum[Int]
      c <- Gen.posNum[Int]
      s <- Gen.posNum[Int]
    } yield VisaRestrictionsIndex(Ranking(r), Count(c), Sharing(s))
  }

  implicit val country: Arbitrary[Country] = Arbitrary[Country] {
    for {
      c <- Gen.alphaUpperStr
      n <- Gen.alphaStr
      u <- Gen.alphaStr
    } yield Country(CountryCode(c), CountryName(n), Currency(u))
  }

  implicit val visaCategory: Arbitrary[VisaCategory] = Arbitrary[VisaCategory] {
    val list = List(
      VisaNotRequired, VisaWaiverProgram, AdmissionRefused, TravelBanned,
      VisaRequired, VisaDeFactoRequired, ElectronicVisa, ElectronicVisitor,
      ElectronicTravelAuthority, FreeVisaOnArrival, VisaOnArrival,
      ElectronicVisaPlusVisaOnArrival, OnlineReciprocityFee,
      MainlandTravelPermit, HomeReturnPermitOnly
    )
    Gen.oneOf(list)
  }

  implicit val visaRequirementsData: Arbitrary[VisaRequirementsData] = Arbitrary[VisaRequirementsData] {
    for {
      from <- arbitrary[Country]
      to   <- arbitrary[Country]
      cat  <- arbitrary[VisaCategory]
      desc <- Gen.alphaStr
    } yield VisaRequirementsData(from, to, cat, desc)
  }

  implicit val diseaseCategory: Arbitrary[DiseaseCategory] = Arbitrary[DiseaseCategory] {
    val list = List(
      AvoidNonSterileEquipment, TakeAntimalarialMeds, GetVaccinated,
      AvoidSharingBodyFluids, ReduceExposureToGerms, EatAndDrinkSafely,
      PreventBugBites, KeepAwayFromAnimals
    )
    Gen.oneOf(list)
  }

  implicit val vaccine: Arbitrary[Vaccine] = Arbitrary[Vaccine] {
    for {
      d <- Gen.alphaStr
      x <- Gen.alphaStr
      c <- Gen.listOf(arbitrary[DiseaseCategory])
    } yield {
      val categories = if (c.isEmpty) List(UnknownDiseaseCategory) else c
      Vaccine(Disease(d), x, categories)
    }
  }

  implicit val healthAlert: Arbitrary[HealthAlert] = Arbitrary[HealthAlert] {
    for {
      t <- Gen.alphaStr
      w <- Gen.alphaStr
      d <- Gen.alphaStr
    } yield HealthAlert(t, WebLink(w), d)
  }

  implicit val alertLevel: Arbitrary[AlertLevel] = Arbitrary[AlertLevel] {
    Gen.oneOf(LevelOne, LevelTwo)
  }

}
