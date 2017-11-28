package com.github.gvolpe.smartbackpacker.service

import cats.effect.{Effect, Sync}
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.parser.AbstractWikiPageParser

class CountryService[F[_] : Effect](wikiPageParser: AbstractWikiPageParser[F], exchangeRateService: AbstractExchangeRateService[F]) {

  def destinationInformation(from: CountryCode, to: CountryCode, baseCurrency: Currency): F[DestinationInfo] =
    validateCountries(from, to).flatMap { _ =>
      val foreignCurrency = SBConfiguration.countryCurrency(to).getOrElse("EUR").as[Currency]

      (visaRequirementsFor(from, to), exchangeRateService.exchangeRateFor(baseCurrency, foreignCurrency)).mapN { (vr, er) =>
        DestinationInfo(
          countryName = vr.country.as[CountryName],
          countryCode = to,
          visaRequirements = VisaRequirements(vr.visaCategory, vr.description),
          exchangeRate = ExchangeRate(er.base.as[Currency], foreignCurrency, er.rates.getOrElse(foreignCurrency.value, -1.0))
        )
      }
    }

  private def validateCountries(from: CountryCode, to: CountryCode): F[(CountryCode, CountryCode)] = {
    if (from != to) (from, to).pure[F]
    else Sync[F].raiseError(CountriesMustBeDifferent)
  }

  private def visaRequirementsFor(from: CountryCode, to: CountryCode): F[VisaRequirementsForDELETEME] = {
    val ifEmpty = Sync[F].raiseError[VisaRequirementsForDELETEME](CountryNotFound(to))

    // Some country names have different ways to be spelled. Eg. ["Côte d'Ivoire", "Ivory Coast"]
    def iter(countryNames: List[String]): F[VisaRequirementsForDELETEME] = countryNames match {
      case (country :: Nil) =>
        wikiPageParser.visaRequirementsFor(from, country.as[CountryName])
      case (country :: xs)  =>
        wikiPageParser.visaRequirementsFor(from, country.as[CountryName]).>>= { result =>
          if (result.visaCategory == UnknownVisaCategory) iter(xs)
          else result.pure[F]
        }
      case Nil              => ifEmpty
    }

    iter(SBConfiguration.countryNames(to))
  }

}