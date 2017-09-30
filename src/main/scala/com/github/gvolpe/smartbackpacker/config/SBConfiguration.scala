package com.github.gvolpe.smartbackpacker.config

import com.github.gvolpe.smartbackpacker.model.CountryCode
import com.typesafe.config.ConfigFactory

object SBConfiguration {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def wikiPage(countryCode: String): Option[String] = {
    safeConfig.string(s"visa-requirements.page.$countryCode")
  }

  def airlineReviewPage(airlineName: String): Option[String] = {
    safeConfig.string(s"airline-reviews.page.$airlineName")
  }

  def countryName(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"countries.name.$countryCode")
  }

}
