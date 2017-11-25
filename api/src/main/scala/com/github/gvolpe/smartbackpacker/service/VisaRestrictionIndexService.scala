package com.github.gvolpe.smartbackpacker.service

import cats.Functor
import com.github.gvolpe.smartbackpacker.model.{CountryCode, VisaRestrictionsIndex}
import com.github.gvolpe.smartbackpacker.persistence.VisaRestrictionsIndexDao

class VisaRestrictionIndexService[F[_] : Functor](visaRestrictionsIndexDao: VisaRestrictionsIndexDao[F]) {

  def findIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]] = {
    visaRestrictionsIndexDao.findIndex(countryCode)
  }

}
