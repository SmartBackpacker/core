package com.github.gvolpe.smartbackpacker.service

import cats.effect.Sync
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model.{CountryCode, VisaRestrictionIndex}
import com.github.gvolpe.smartbackpacker.parser.{AbstractVisaRestrictionsIndexParser, VisaRestrictionsIndexParser}

object VisaRestrictionIndexService {
  def apply[F[_] : Sync]: VisaRestrictionIndexService[F] = new VisaRestrictionIndexService[F](VisaRestrictionsIndexParser[F])
}

class VisaRestrictionIndexService[F[_] : Sync](visaRestrictionsIndexParser: AbstractVisaRestrictionsIndexParser[F]) {

  def findIndex(countryCode: CountryCode): F[Option[VisaRestrictionIndex]] = {
    val countryName = SBConfiguration.countryNames(countryCode).headOption.getOrElse("")
    visaRestrictionsIndexParser.parse.map { e =>
      e.find(_.countries.contains(countryName))
    }
  }

}
