package com.github.gvolpe.smartbackpacker.config

import com.github.gvolpe.smartbackpacker.model._
import com.typesafe.config.ConfigFactory

object SBConfiguration {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def fixerBaseUri: Option[String] = {
    sys.env.get("FIXER_URL").orElse(safeConfig.string("fixer.uri"))
  }

  def countryCurrency(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"countries.currency.${countryCode.value}")
  }

}
