package com.github.gvolpe.smartbackpacker.scraper

object model {

  case class VisaRestrictionsRanking(rank: Int, countries: List[String], count: Int)

}
