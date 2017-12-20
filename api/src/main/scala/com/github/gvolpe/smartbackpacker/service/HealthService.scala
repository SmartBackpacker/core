package com.github.gvolpe.smartbackpacker.service

import com.github.gvolpe.smartbackpacker.model.{CountryCode, Health}
import com.github.gvolpe.smartbackpacker.repository.algebra.HealthRepository

class HealthService[F[_]](healthRepo: HealthRepository[F]) {

  def findHealthInfo(countryCode: CountryCode): F[Option[Health]] = {
    healthRepo.findHealthInfo(countryCode)
  }

}
