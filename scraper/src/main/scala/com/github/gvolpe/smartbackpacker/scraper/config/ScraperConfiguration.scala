package com.github.gvolpe.smartbackpacker.scraper.config

import cats.effect.Sync
import com.github.gvolpe.smartbackpacker.config.SafeConfigReader
import com.github.gvolpe.smartbackpacker.model._
import com.typesafe.config.ConfigFactory

class ScraperConfiguration[F[_]](implicit F: Sync[F]) {

  private lazy val configuration  = ConfigFactory.load("sb-scraper")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def wikiPage(countryCode: CountryCode): F[Option[String]] = F.delay {
    safeConfig.string(s"visa-requirements.page.${countryCode.value}")
  }

  def healthPage(countryCode: CountryCode): F[Option[String]] = F.delay {
    safeConfig.string(s"health.page.${countryCode.value}")
  }

  def countries(): F[List[Country]] = F.delay {
    safeConfig.objectMap("countries.name").map(kv =>
      (kv._1.as[CountryCode], kv._2.headOption.getOrElse("Empty").as[CountryName])
    ).toList.map(kv => Country(kv._1, kv._2)).sortBy(_.code.value)
  }

  def countriesWithNames(): F[List[CountryWithNames]] = F.delay {
    safeConfig.objectMap("countries.name").map(kv =>
      (kv._1.as[CountryCode], kv._2.map(_.as[CountryName]))
    ).toList.map(kv => CountryWithNames(kv._1, kv._2)).sortBy(_.code.value)
  }

  def countriesCode(): F[List[CountryCode]] = F.delay {
    safeConfig.objectKeyList("countries.name").sorted.map(_.as[CountryCode])
  }

  def countryNames(countryCode: CountryCode): F[List[String]] = F.delay {
    safeConfig.list(s"countries.name.${countryCode.value}")
  }

}
