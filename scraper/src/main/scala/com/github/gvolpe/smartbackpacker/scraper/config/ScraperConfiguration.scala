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

  def schengen(): F[List[CountryCode]] = F.delay {
    safeConfig.list("countries.schengen").map(_.as[CountryCode])
  }

  def countries(): F[List[Country]] = F.delay {
    val names = safeConfig.objectMapOfList("countries.name").map {
      case ((co, ns)) => (co.as[CountryCode], ns.headOption.getOrElse("Empty").as[CountryName])
    }.toList.sortBy(_._1.value)

    val currencies = safeConfig.objectMap("countries.currency").map {
      case ((co, cu)) => (co.as[CountryCode], cu.as[Currency])
    }.toList.sortBy(_._1.value)

    names.zip(currencies).flatMap {
      case ((co, cn), (co2, cu)) if co.value == co2.value =>
        List(Country(co, cn, cu))
      case _ => List.empty[Country]
    }.sortBy(_.code.value)
  }

  def countriesWithNames(): F[List[CountryWithNames]] = F.delay {
    safeConfig.objectMapOfList("countries.name").map {
      case ((co, ns)) => CountryWithNames(co.as[CountryCode], ns.map(_.as[CountryName]))
    }.toList.sortBy(_.code.value)
  }

  def countriesCode(): F[List[CountryCode]] = F.delay {
    safeConfig.objectKeyList("countries.name").sorted.map(_.as[CountryCode])
  }

  def countryNames(countryCode: CountryCode): F[List[String]] = F.delay {
    safeConfig.list(s"countries.name.${countryCode.value}")
  }

}
