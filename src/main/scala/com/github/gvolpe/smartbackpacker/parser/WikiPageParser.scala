package com.github.gvolpe.smartbackpacker.parser

import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Document

object WikiPageParser extends WikiPageParser {

  // TODO: Make this request asynchronous
  override def htmlDocument = {
    val browser = new JsoupBrowser()
    val wikiPage = SBConfiguration.wikiPage("AR").getOrElse("http://google.com")
    browser.get(wikiPage)
  }

}

trait WikiPageParser {

  def htmlDocument: Document

  // TODO: From country is now defined at htmlDocument, change this...
  def visaRequirementsFor(from: CountryCode, to: CountryCode): VisaRequirements = {
    // TODO: Encapsulate the operation in an F[_]
    parseVisaRequirements.find(_.country == to.toLowerCase().capitalize).get
  }

  def parseVisaRequirements: List[VisaRequirements] = {
    val table = htmlDocument >> extractor(".sortable td", texts)

    table.toList.grouped(3).map { seq =>
      VisaRequirements(seq.head.asCountry, seq(1).asVisaCategory, seq(2).asDescription)
    }.toList
  }

}