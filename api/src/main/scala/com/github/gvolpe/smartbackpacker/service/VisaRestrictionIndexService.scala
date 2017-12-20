package com.github.gvolpe.smartbackpacker.service

import cats.Functor
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model.{CountryCode, VisaRestrictionsIndex}
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRestrictionsIndexRepository

class VisaRestrictionIndexService[F[_] : Functor](visaRestrictionsIndexRepo: VisaRestrictionsIndexRepository[F]) {

  def findIndex(countryCode: CountryCode): F[ValidationError Either VisaRestrictionsIndex] =
    visaRestrictionsIndexRepo.findRestrictionsIndex(countryCode) map { index =>
      index.toRight[ValidationError](VisaRestrictionsIndexNotFound(countryCode))
    }

}
