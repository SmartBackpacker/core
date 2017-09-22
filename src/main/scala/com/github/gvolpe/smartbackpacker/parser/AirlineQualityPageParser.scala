package com.github.gvolpe.smartbackpacker.parser

import com.github.gvolpe.smartbackpacker.model._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Document

trait AirlineQualityPageParser {

  def htmlDocument: Document

  def parseAirlineReviews: Unit = {
    println(htmlDocument)
    val table = htmlDocument >> extractor(".review-ratings td", texts)

    table.foreach(println)
  }

}
