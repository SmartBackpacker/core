package com.github.gvolpe.smartbackpacker.service

import cats.MonadError
import cats.data.EitherT
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRequirementsRepository

class CountryService[F[_]](visaRequirementsRepo: VisaRequirementsRepository[F],
                           exchangeRateService: AbstractExchangeRateService[F])
                          (implicit F: MonadError[F, Throwable]) {

  def destinationInformation(from: CountryCode, to: CountryCode, baseCurrency: Currency): F[ValidationError Either DestinationInfo] = {
    val result = EitherT.fromEither(validateCountries(from, to)) flatMap { _ =>
      val foreignCurrency = SBConfiguration.countryCurrency(to).getOrElse("EUR").as[Currency]
      EitherT(
        (visaRequirementsFor(from, to), exchangeRateService.exchangeRateFor(baseCurrency, foreignCurrency)).mapN {
          case (Right(vr), er) =>
            DestinationInfo(
              countryName = vr.to.name,
              countryCode = vr.to.code,
              visaRequirements = VisaRequirements(vr.visaCategory, vr.description),
              exchangeRate = ExchangeRate(er.base.as[Currency], foreignCurrency, er.rates.getOrElse(foreignCurrency.value, -1.0))
            ).asRight[ValidationError]
          case (Left(e), _) =>
            e.asLeft[DestinationInfo]
        }
      )
    }
    result.value
  }

  private def validateCountries(from: CountryCode, to: CountryCode): Either[ValidationError, (CountryCode, CountryCode)] = {
    if (from != to) (from, to).asRight
    else CountriesMustBeDifferent.asLeft
  }

  private def visaRequirementsFor(from: CountryCode, to: CountryCode): F[ValidationError Either VisaRequirementsData] =
    visaRequirementsRepo.findVisaRequirements(from, to) map { data =>
      data.toRight[ValidationError](CountryNotFound(to))
    }

}