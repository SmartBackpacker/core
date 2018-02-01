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

package com.smartbackpackerapp.scraper.sql

import com.smartbackpackerapp.model._
import com.smartbackpackerapp.scraper.model.VisaRequirementsFor
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks

class ConversionsSpec extends FunSuite with ConversionsArbitraries with PropertyChecks {

  forAll { (vr: VisaRequirementsFor) =>
    test(s"convert a $vr into a VisaRequirementsDTO") {
      val dto = (vr.from.value, vr.to.value, vr.visaCategory.toString, vr.description)
      assert(vr.toVisaRequirementsDTO == dto)
    }
  }

  forAll { (v: Vaccine) =>
    test(s"convert a $v into a VaccineDTO # ${v.hashCode()}") {
      val dto = (v.disease.value, v.description, v.diseaseCategories.map(_.toString).mkString(","))
      assert(v.toVaccineDTO == dto)
    }
  }

}

trait ConversionsArbitraries {

  implicit val visaCategory: Arbitrary[VisaCategory] = Arbitrary[VisaCategory] {
    val list = List(
      VisaNotRequired, VisaWaiverProgram, AdmissionRefused, TravelBanned,
      VisaRequired, VisaDeFactoRequired, ElectronicVisa, ElectronicVisitor,
      ElectronicTravelAuthority, FreeVisaOnArrival, VisaOnArrival,
      ElectronicVisaPlusVisaOnArrival, OnlineReciprocityFee,
      MainlandTravelPermit, HomeReturnPermitOnly, UnknownVisaCategory
    )
    Gen.oneOf(list)
  }

  implicit val visaRequirementsFor: Arbitrary[VisaRequirementsFor] = Arbitrary[VisaRequirementsFor] {
    for {
      f <- Gen.alphaUpperStr
      t <- Gen.alphaUpperStr
      c <- arbitrary[VisaCategory]
      d <- Gen.alphaStr
    } yield VisaRequirementsFor(CountryCode(f), CountryCode(t), c, d)
  }

  implicit val diseaseCategory: Arbitrary[DiseaseCategory] = Arbitrary[DiseaseCategory] {
    val list = List(
      AvoidNonSterileEquipment, TakeAntimalarialMeds, GetVaccinated,
      AvoidSharingBodyFluids, ReduceExposureToGerms, EatAndDrinkSafely,
      PreventBugBites, KeepAwayFromAnimals, UnknownDiseaseCategory
    )
    Gen.oneOf(list)
  }

  implicit val vaccine: Arbitrary[Vaccine] = Arbitrary[Vaccine] {
    for {
      d <- Gen.alphaStr
      x <- Gen.alphaStr
      c <- Gen.listOf(arbitrary[DiseaseCategory])
    } yield Vaccine(Disease(d), x, c)
  }

}
