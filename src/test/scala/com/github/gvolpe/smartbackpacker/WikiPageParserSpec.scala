package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model.UnknownVisaCategory
import com.github.gvolpe.smartbackpacker.parser.WikiPageParser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.{FlatSpecLike, Matchers}

import scala.io.Source

// TODO: Re-work this test
class WikiPageParserSpec extends FlatSpecLike with Matchers {

//  object ForTestWikiPageParser extends WikiPageParser {
//    override def htmlDocument = {
//      val browser = JsoupBrowser()
//      val fileContent = Source.fromResource("wikiPageTest.html").mkString
//      browser.parseString(fileContent).asInstanceOf[Document]
//    }
//  }
//
//  it should "parse a 'Visa Requirements for [COUNTRY] citizens' page" in {
//    val requirements = ForTestWikiPageParser.parseVisaRequirements
//    requirements should not be empty
//    requirements should have size 193
//    requirements foreach { e =>
//      e.visaCategory  should not be UnknownVisaCategory
//      e.description   should not be empty
//    }
//  }

}