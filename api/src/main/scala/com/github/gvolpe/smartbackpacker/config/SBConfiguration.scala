package com.github.gvolpe.smartbackpacker.config

import cats.effect.Sync
import com.github.gvolpe.smartbackpacker.model._
import com.typesafe.config.ConfigFactory

class SBConfiguration[F[_]](implicit F: Sync[F]) {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def fixerBaseUri: F[Option[String]] = F.delay {
    sys.env.get("FIXER_URL").orElse(safeConfig.string("fixer.uri"))
  }

  def countryCurrency(countryCode: CountryCode, default: Currency): F[Currency] = F.delay {
    safeConfig.string(s"countries.currency.${countryCode.value}").map(_.as[Currency]).getOrElse(default)
  }

}
