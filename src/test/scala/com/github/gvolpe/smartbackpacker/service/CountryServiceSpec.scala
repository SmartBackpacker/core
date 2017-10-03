package com.github.gvolpe.smartbackpacker.service

import cats.effect.IO
import com.github.gvolpe.smartbackpacker._
import com.github.gvolpe.smartbackpacker.model.{ExchangeRate, VisaNotRequired, VisaRequirements}
import org.scalatest.{FlatSpecLike, Matchers}

class CountryServiceSpec extends FlatSpecLike with Matchers {

  behavior of "CountryService"

  object MockCountryService extends CountryService[IO](TestWikiPageParser, TestExchangeRateService)

  it should "retrieve destination information" in {
    val service = MockCountryService

    val info = service.destinationInformation("AR", "RO", "EUR").unsafeRunSync()
    info.countryCode      should be ("RO")
    info.countryName      should be ("Romania")
    info.exchangeRate     should be (ExchangeRate("EUR", "RON", "", 4.59))
    info.visaRequirements should be (VisaRequirements(VisaNotRequired, "90 days within any 180 day period"))
  }

  it should "retrieve destination information with empty exchange rate when currencies are the same" in {
    val service = MockCountryService

    val info = service.destinationInformation("DE", "IE", "EUR").unsafeRunSync()
    info.countryCode      should be ("IE")
    info.countryName      should be ("Ireland")
    info.exchangeRate     should be (ExchangeRate("EUR", "EUR", "", 0.0))
    info.visaRequirements should be (VisaRequirements(VisaNotRequired, "3 months"))
  }

  it should "validate countries" in {
    val service = MockCountryService

    val info = service.destinationInformation("AR", "AR", "EUR").attempt.unsafeRunSync()
    info should be (Left(CountriesMustBeDifferent))
  }

  // TODO: Add test for all the countries

}
