package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.parser.AirlineQualityPageParser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

object SmartBackpacker extends App with AirlineQualityPageParser {

  println("Smart Backpacker API")

  val airlineQualityPage = SBConfiguration.airlineReviewPage("RyanAir").getOrElse("http://google.com")

  val userAgent  ="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36"

  val browser = new JsoupBrowser(userAgent)
  override val htmlDocument = browser.get(airlineQualityPage)

  parseAirlineReviews

//  val list = parseVisaRequirements
//
//  list.foreach(println)

}
