package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.parser.WikiPageParser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

object SmartBackpacker extends App with WikiPageParser {

  println("Smart Backpacker API")

  val wikiPage = SBConfiguration.page("Argentina").getOrElse("http://google.com")

  val browser = JsoupBrowser()
  override val htmlDocument = browser.get(wikiPage)

  val list = parseVisaRequirements

  list.foreach(println)

}
