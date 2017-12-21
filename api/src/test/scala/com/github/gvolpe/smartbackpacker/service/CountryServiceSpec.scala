package com.github.gvolpe.smartbackpacker.service

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.option._
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRequirementsRepository
import org.scalatest.{FlatSpecLike, Matchers}

class CountryServiceSpec extends FlatSpecLike with Matchers {

  private lazy val sbConfig = new SBConfiguration[IO]

  private val repo = new VisaRequirementsRepository[IO] {
    override def findVisaRequirements(from: CountryCode, to: CountryCode): IO[Option[VisaRequirementsData]] = IO {
      VisaRequirementsData(
        from = Country("AR".as[CountryCode], "Argentina".as[CountryName]),
        to   = Country("RO".as[CountryCode], "Romania".as[CountryName]),
        visaCategory = VisaNotRequired,
        description = "90 days within any 180 day period"
      ).some
    }
  }

  private val rateService = new AbstractExchangeRateService[IO](sbConfig) {
    override protected def retrieveExchangeRate(uri: String): IO[CurrencyExchangeDTO] = IO {
      CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59))
    }
  }

  object MockCountryService extends CountryService[IO](sbConfig, repo, rateService)

  private val service = MockCountryService

  it should "retrieve destination information" in IOAssertion {
    EitherT(service.destinationInformation("AR".as[CountryCode], "RO".as[CountryCode], "EUR".as[Currency])).map { info =>
      info.countryCode.value  should be ("RO")
      info.countryName.value  should be ("Romania")
      info.exchangeRate       should be (ExchangeRate("EUR".as[Currency], "RON".as[Currency], 4.59))
      info.visaRequirements   should be (VisaRequirements(VisaNotRequired, "90 days within any 180 day period"))
    }.value
  }

  it should "validate countries" in IOAssertion {
    EitherT(service.destinationInformation("AR".as[CountryCode], "AR".as[CountryCode], "EUR".as[Currency])).leftMap { error =>
      error should be (CountriesMustBeDifferent)
    }.value
  }

}
