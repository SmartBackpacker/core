package com.github.gvolpe.smartbackpacker.service

import cats.MonadError
import cats.data.EitherT
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRequirementsRepository

class DestinationInfoService[F[_]](sbConfig: SBConfiguration[F],
                                   visaRequirementsRepo: VisaRequirementsRepository[F],
                                   exchangeRateService: AbstractExchangeRateService[F])
                                  (implicit F: MonadError[F, Throwable]) {

  def find(from: CountryCode,
           to: CountryCode,
           baseCurrency: Currency): F[ValidationError Either DestinationInfo] = {
    val result =
      for {
        _  <- EitherT.fromEither(validateCountries(from, to))
        fc <- EitherT.liftF(sbConfig.countryCurrency(to, default = "EUR".as[Currency]))
        rs <- EitherT(retrieveDestinationInfoInParallel(from, to, baseCurrency, fc))
      } yield rs

    result.value
  }

  private def retrieveDestinationInfoInParallel(from: CountryCode,
                                                to: CountryCode,
                                                baseCurrency: Currency,
                                                foreignCurrency: Currency): F[ValidationError Either DestinationInfo] = {
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