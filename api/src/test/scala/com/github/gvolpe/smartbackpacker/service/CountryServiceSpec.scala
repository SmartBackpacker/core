package com.github.gvolpe.smartbackpacker.service

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker._
import com.github.gvolpe.smartbackpacker.model._
import org.scalatest.{FlatSpecLike, Matchers}

class CountryServiceSpec extends FlatSpecLike with Matchers {

  object MockCountryService extends CountryService[IO](TestWikiPageParser, TestExchangeRateService)

  private val service = MockCountryService

  it should "retrieve destination information" in IOAssertion {
    service.destinationInformation("AR".as[CountryCode], "RO".as[CountryCode], "EUR".as[Currency]).map { info =>
      info.countryCode.value  should be ("RO")
      info.countryName.value  should be ("Romania")
      info.exchangeRate       should be (ExchangeRate("EUR".as[Currency], "RON".as[Currency], 4.59))
      info.visaRequirements   should be (VisaRequirements(VisaNotRequired, "90 days within any 180 day period"))
    }
  }

  it should "retrieve destination information with empty exchange rate when currencies are the same" in IOAssertion {
    service.destinationInformation("DE".as[CountryCode], "IE".as[CountryCode], "EUR".as[Currency]).map { info =>
      info.countryCode.value  should be ("IE")
      info.countryName.value  should be ("Ireland")
      info.exchangeRate       should be (ExchangeRate("EUR".as[Currency], "EUR".as[Currency], 0.0))
      info.visaRequirements   should be (VisaRequirements(VisaNotRequired, "Freedom of movement; ID card valid"))
    }
  }

  it should "validate countries" in IOAssertion {
    service.destinationInformation("AR".as[CountryCode], "AR".as[CountryCode], "EUR".as[Currency]).attempt.map { info =>
      info should be (Left(CountriesMustBeDifferent))
    }
  }

}
