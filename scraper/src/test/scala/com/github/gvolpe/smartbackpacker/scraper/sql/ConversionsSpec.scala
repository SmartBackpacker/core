package com.github.gvolpe.smartbackpacker.scraper.sql

import com.github.gvolpe.smartbackpacker.model._
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

}

trait ConversionsArbitraries {

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

  implicit val visaRequirementsFor: Arbitrary[VisaRequirementsFor] = Arbitrary[VisaRequirementsFor] {
    for {
      f <- Gen.alphaUpperStr
      t <- Gen.alphaUpperStr
      c <- arbitrary[VisaCategory]
      d <- Gen.alphaStr
    } yield VisaRequirementsFor(f.as[CountryCode], t.as[CountryCode], c, d)
  }

}
