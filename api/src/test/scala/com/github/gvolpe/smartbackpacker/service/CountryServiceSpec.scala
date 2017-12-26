package com.github.gvolpe.smartbackpacker.service

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.CountryRepository
import org.scalatest.{FlatSpecLike, Matchers}

class CountryServiceSpec extends FlatSpecLike with Matchers {

  private val testCountries = List(
    Country("AR".as[CountryCode], "Argentina".as[CountryName], "ARS".as[Currency])
  )

  private val testSchengenCountries = List(
    Country("PL".as[CountryCode], "Poland".as[CountryName], "PLN".as[Currency])
  )

  private val repo = new CountryRepository[IO] {
    override def findAll: IO[List[Country]] = IO(testCountries)
    override def findSchengen: IO[List[Country]] = IO(testSchengenCountries)
  }

  private val service = new CountryService[IO](repo)

  it should "find all the countries" in IOAssertion {
    service.findAll(false).map { countries =>
      countries should be (testCountries)
    }
  }

  it should "find all schengen the countries" in IOAssertion {
    service.findAll(true).map { countries =>
      countries should be (testSchengenCountries)
    }
  }

}
