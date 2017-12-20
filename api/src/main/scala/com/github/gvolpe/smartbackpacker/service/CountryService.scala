package com.github.gvolpe.smartbackpacker.service

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRequirementsRepository

class CountryService[F[_]](visaRequirementsRepo: VisaRequirementsRepository[F],
                           exchangeRateService: AbstractExchangeRateService[F])
                          (implicit F: MonadError[F, Throwable]) {

  def destinationInformation(from: CountryCode, to: CountryCode, baseCurrency: Currency): F[DestinationInfo] =
    validateCountries(from, to) flatMap { _ =>
      val foreignCurrency = SBConfiguration.countryCurrency(to).getOrElse("EUR").as[Currency]

      (visaRequirementsFor(from, to), exchangeRateService.exchangeRateFor(baseCurrency, foreignCurrency)).mapN { (vr, er) =>
        DestinationInfo(
          countryName = vr.to.name,
          countryCode = vr.to.code,
          visaRequirements = VisaRequirements(vr.visaCategory, vr.description),
          exchangeRate = ExchangeRate(er.base.as[Currency], foreignCurrency, er.rates.getOrElse(foreignCurrency.value, -1.0))
        )
      }
    }

  private def validateCountries(from: CountryCode, to: CountryCode): F[(CountryCode, CountryCode)] = {
    if (from != to) (from, to).pure[F]
    else F.raiseError(CountriesMustBeDifferent)
  }

  private def visaRequirementsFor(from: CountryCode, to: CountryCode): F[VisaRequirementsData] = {
    val ifEmpty = F.raiseError[VisaRequirementsData](CountryNotFound(to))
    visaRequirementsRepo.find(from, to).flatMap { maybeData =>
      maybeData.fold(ifEmpty)(_.pure[F])
    }
  }

}