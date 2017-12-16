package com.github.gvolpe.smartbackpacker.scraper.config

import com.github.gvolpe.smartbackpacker.config.SafeConfigReader
import com.github.gvolpe.smartbackpacker.model._
import com.typesafe.config.ConfigFactory

object ScraperConfiguration {

  private lazy val configuration  = ConfigFactory.load("sb-scraper")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def wikiPage(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"visa-requirements.page.${countryCode.value}")
  }

  def healthPage(countryCode: CountryCode): Option[String] = {
    safeConfig.string(s"health.page.${countryCode.value}")
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

}
