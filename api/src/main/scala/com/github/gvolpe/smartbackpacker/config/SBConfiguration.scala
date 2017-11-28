package com.github.gvolpe.smartbackpacker.config

import com.github.gvolpe.smartbackpacker.model._
import com.typesafe.config.ConfigFactory

object SBConfiguration {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def fixerBaseUri: Option[String] = {
    sys.env.get("FIXER_URL").orElse(safeConfig.string("fixer.uri"))
  }

  def wikiPage(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"visa-requirements.page.${countryCode.value}")
  }

  def countries(): List[Country] = {
    safeConfig.objectMap("countries.name").map(kv =>
      (kv._1.as[CountryCode], kv._2.headOption.getOrElse("Empty").as[CountryName])
    ).toList.map(kv => Country(kv._1, kv._2)).sortBy(_.code.value)
  }

  def countriesWithNames(): List[CountryWithNames] = {
    safeConfig.objectMap("countries.name").map(kv =>
      (kv._1.as[CountryCode], kv._2.map(_.as[CountryName]))
    ).toList.map(kv => CountryWithNames(kv._1, kv._2)).sortBy(_.code.value)
  }

  def countriesCode(): List[CountryCode] = {
    safeConfig.objectKeyList("countries.name").sorted.map(_.as[CountryCode])
  }

  def countryNames(countryCode: CountryCode): List[String] = {
    safeConfig.list(s"countries.name.${countryCode.value}")
  }

  def countryCurrency(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"countries.currency.${countryCode.value}")
  }

}
