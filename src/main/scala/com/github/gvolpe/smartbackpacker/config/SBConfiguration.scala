package com.github.gvolpe.smartbackpacker.config

import com.typesafe.config.ConfigFactory

object SBConfiguration {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def wikiPage(countryName: String): Option[String] = {
    safeConfig.string(s"visa-requirements.page.$countryName")
  }

  def airlineQualityPage(airlineName: String): Option[String] = {
    safeConfig.string(s"airline-reviews.page.$airlineName")
  }

}
