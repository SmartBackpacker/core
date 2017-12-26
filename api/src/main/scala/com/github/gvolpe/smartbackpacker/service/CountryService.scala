package com.github.gvolpe.smartbackpacker.service

import com.github.gvolpe.smartbackpacker.model.Country
import com.github.gvolpe.smartbackpacker.repository.algebra.CountryRepository

class CountryService[F[_]](countryRepo: CountryRepository[F]) {

  def findAll(isSchengen: Boolean): F[List[Country]] = {
    if (isSchengen) countryRepo.findSchengen
    else countryRepo.findAll
  }

}
