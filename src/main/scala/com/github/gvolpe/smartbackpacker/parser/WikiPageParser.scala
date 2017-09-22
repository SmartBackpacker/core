package com.github.gvolpe.smartbackpacker.parser

import com.github.gvolpe.smartbackpacker.model._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Document

trait WikiPageParser {

  def htmlDocument: Document

  def parseVisaRequirements: List[VisaRequirements] = {
    val table = htmlDocument >> extractor(".sortable td", texts)

    table.toList.grouped(3).map { seq =>
      VisaRequirements(seq.head.asCountry, seq(1).asVisaCategory, seq(2).asDescription)
    }.toList
  }

}