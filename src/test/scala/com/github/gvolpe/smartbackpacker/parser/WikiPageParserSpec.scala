package com.github.gvolpe.smartbackpacker.parser

import com.github.gvolpe.smartbackpacker.TestWikiPageParser
import com.github.gvolpe.smartbackpacker.model.{UnknownVisaCategory, VisaNotRequired}
import org.scalatest.{FlatSpecLike, Matchers}

class WikiPageParserSpec extends FlatSpecLike with Matchers {

  behavior of "WikiPageParser"

  it should "find Visa Requirements" in {
    val requirements = TestWikiPageParser.visaRequirementsFor("AR", "Romania").unsafeRunSync()
    requirements.visaCategory  should be (VisaNotRequired)
    requirements.description   should be ("90 days within any 180 day period")
  }

  it should "NOT find Visa Requirements" in {
    val requirements = TestWikiPageParser.visaRequirementsFor("AR", "Whatever").unsafeRunSync()
    requirements.visaCategory  should be (UnknownVisaCategory)
    requirements.description   should be ("No information available")
  }

}