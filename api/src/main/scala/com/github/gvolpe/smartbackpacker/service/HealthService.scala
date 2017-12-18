package com.github.gvolpe.smartbackpacker.service

import com.github.gvolpe.smartbackpacker.model.{CountryCode, Health}
import com.github.gvolpe.smartbackpacker.persistence.HealthDao

class HealthService[F[_]](healthDao: HealthDao[F]) {

  def findHealthInfo(countryCode: CountryCode): F[Option[Health]] = {
    healthDao.findHealthInfo(countryCode)
  }

}
