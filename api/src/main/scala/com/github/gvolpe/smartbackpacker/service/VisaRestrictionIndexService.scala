package com.github.gvolpe.smartbackpacker.service

import com.github.gvolpe.smartbackpacker.model.{CountryCode, VisaRestrictionsIndex}
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRestrictionsIndexRepository

class VisaRestrictionIndexService[F[_]](visaRestrictionsIndexRepo: VisaRestrictionsIndexRepository[F]) {

  def findIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]] = {
    visaRestrictionsIndexRepo.findIndex(countryCode)
  }

}
