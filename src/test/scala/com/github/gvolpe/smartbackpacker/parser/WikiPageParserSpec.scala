package com.github.gvolpe.smartbackpacker.parser

import com.github.gvolpe.smartbackpacker.TestWikiPageParser
import com.github.gvolpe.smartbackpacker.model._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class WikiPageParserSpec extends FlatSpecLike with Matchers with WikiPageParserFixture {

  behavior of "WikiPageParser"

  forAll(examples) { (description, from, to, expectedCategory, expectedDescription) =>
    it should description in {
      val requirements = TestWikiPageParser.visaRequirementsFor(from, to).unsafeRunSync()
      requirements.visaCategory  should be (expectedCategory)
      requirements.description   should be (expectedDescription)
    }
  }

}

trait WikiPageParserFixture extends PropertyChecks {

  val examples = Table(
    ("description", "from", "to", "expectedCategory", "expectedDescription"),
    ("find VisaNotRequired in Visa Requirements", "AR", "Romania", VisaNotRequired, "90 days within any 180 day period"),
    ("find VisaWaiverProgram in Visa Requirements", "IE", "United States", VisaWaiverProgram, "90 days"),
    ("find VisaRequired in Visa Requirements", "AR", "Cameroon", VisaRequired, "No information available"),
    ("find VisaOnArrival in Visa Requirements", "AR", "Egypt", VisaOnArrival, "30 days"),
    ("find ElectronicVisa in Visa Requirements", "AR", "India", ElectronicVisa, "60 days; e-Visa holders must arrive via 24 designated airports or 3 designated seaports.[92]"),
    ("find ElectronicVisaPlusOnArrival in Visa Requirements", "AR", "Ethiopia", ElectronicVisaPlusOnArrival, "No information available"),
    ("find ElectronicVisitor in Visa Requirements", "DE", "Australia", ElectronicVisitor, "90 days on each visit in 12-month period if granted"),
    ("find FreeVisaOnArrival in Visa Requirements", "DE", "Papua New Guinea", FreeVisaOnArrival, "60 days. Free of charge."),
    ("find VisaDeFactoRequired in Visa Requirements", "FJ", "Andorra", VisaDeFactoRequired, "There are no visa requirements for entry into Andorra, but it can only be accessed by passing through France or Spain. A multiple entry visa is required to re-enter either France or Spain when leaving Andorra. All visitors can stay for 3 months."),
    ("find AdmissionRefused in Visa Requirements", "GN", "Jordan", AdmissionRefused, "No information available"),
    ("NOT find Visa Requirements (UnknownVisaCategory)", "AR", "Mars", UnknownVisaCategory, "No information available")
  )

}