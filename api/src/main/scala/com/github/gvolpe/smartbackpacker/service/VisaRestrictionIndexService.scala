package com.github.gvolpe.smartbackpacker.service

import com.github.gvolpe.smartbackpacker.model.{CountryCode, VisaRestrictionsIndex}
import com.github.gvolpe.smartbackpacker.persistence.VisaRestrictionsIndexDao

class VisaRestrictionIndexService[F[_]](visaRestrictionsIndexDao: VisaRestrictionsIndexDao[F]) {

  def findIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]] = {
    visaRestrictionsIndexDao.findIndex(countryCode)
  }

}
