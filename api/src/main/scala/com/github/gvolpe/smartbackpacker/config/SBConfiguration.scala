package com.github.gvolpe.smartbackpacker.config

import com.github.gvolpe.smartbackpacker.model.CountryCode
import com.typesafe.config.ConfigFactory

object SBConfiguration {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def wikiPage(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"visa-requirements.page.${countryCode.value}")
  }

  def airlineReviewPage(airlineName: String): Option[String] = {
    safeConfig.string(s"airline-reviews.page.$airlineName")
  }

  def countryNames(countryCode: CountryCode): List[String] = {
    safeConfig.list(s"countries.name.${countryCode.value}")
  }

  def countryCurrency(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"countries.currency.${countryCode.value}")
  }

}
