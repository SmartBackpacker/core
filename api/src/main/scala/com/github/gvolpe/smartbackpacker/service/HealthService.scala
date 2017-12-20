package com.github.gvolpe.smartbackpacker.service

import cats.Functor
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model.{CountryCode, Health}
import com.github.gvolpe.smartbackpacker.repository.algebra.HealthRepository

class HealthService[F[_] : Functor](healthRepo: HealthRepository[F]) {

  def findHealthInfo(countryCode: CountryCode): F[ValidationError Either Health] =
    healthRepo.findHealthInfo(countryCode) map { health =>
      health.toRight[ValidationError](HealthInfoNotFound(countryCode))
    }

}
