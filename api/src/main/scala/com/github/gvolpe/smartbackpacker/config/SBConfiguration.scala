package com.github.gvolpe.smartbackpacker.config

import com.github.gvolpe.smartbackpacker.model.CountryCode
import com.typesafe.config.ConfigFactory

object SBConfiguration {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def fixerBaseUri: Option[String] = {
    safeConfig.string("fixer.uri")
  }

  def wikiPage(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"visa-requirements.page.${countryCode.value}")
  }

  def countriesCode(): List[String] = {
    safeConfig.objectKeyList(s"countries.name").sorted
  }

  def countryNames(countryCode: CountryCode): List[String] = {
    safeConfig.list(s"countries.name.${countryCode.value}")
  }

  def countryCurrency(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"countries.currency.${countryCode.value}")
  }

}
