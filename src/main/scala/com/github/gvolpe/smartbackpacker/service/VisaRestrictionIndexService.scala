package com.github.gvolpe.smartbackpacker.service

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model.{CountryCode, VisaRestrictionIndex}
import com.github.gvolpe.smartbackpacker.parser.VisaRestrictionsIndexParser

object VisaRestrictionIndexService {
  def apply[F[_] : Effect]: VisaRestrictionIndexService[F] = new VisaRestrictionIndexService[F]()
}

class VisaRestrictionIndexService[F[_] : Effect] {

  def findIndex(countryCode: CountryCode): F[Option[VisaRestrictionIndex]] = {
    val countryName = SBConfiguration.countryNames(countryCode).headOption.getOrElse("")
    Effect[F].map(VisaRestrictionsIndexParser[F].parse) { e =>
      e.find(_.countries.contains(countryName))
    }
  }

}
