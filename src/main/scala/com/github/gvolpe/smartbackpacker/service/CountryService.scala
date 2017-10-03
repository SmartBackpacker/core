package com.github.gvolpe.smartbackpacker.service

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model.{CountryCode, CountryNotFound, Currency, DestinationInfo, ExchangeRate, VisaRequirements, VisaRequirementsFor}
import com.github.gvolpe.smartbackpacker.parser.{AbstractWikiPageParser, WikiPageParser}

object CountryService {
  def apply[F[_] : Effect]: CountryService[F] = new CountryService[F](WikiPageParser[F], ExchangeRateService[F])
}

class CountryService[F[_] : Effect](wikiPageParser: AbstractWikiPageParser[F], exchangeRateService: AbstractExchangeRateService[F]) {

  def destinationInformation(from: CountryCode, to: CountryCode, baseCurrency: Currency): F[DestinationInfo] = {
    import cats.syntax.apply._

    Effect[F].flatMap(validateCountries(from, to)) { _ =>
      val foreignCurrency = SBConfiguration.countryCurrency(to).getOrElse("EUR")

      (visaRequirementsFor(from, to), exchangeRateService.exchangeRateFor(baseCurrency, foreignCurrency)).mapN { (vr, er) =>
        DestinationInfo(
          countryName = vr.country,
          countryCode = to,
          visaRequirements = VisaRequirements(vr.visaCategory, vr.description),
          exchangeRate = ExchangeRate(er.base, foreignCurrency, er.date, er.rates.getOrElse(foreignCurrency, 0.0))
        )
      }
    }
  }

  private def validateCountries(from: CountryCode, to: CountryCode): F[(CountryCode, CountryCode)] = {
    if (from != to) Effect[F].delay((from, to))
    else Effect[F].raiseError(CountriesMustBeDifferent)
  }

  private def visaRequirementsFor(from: CountryCode, to: CountryCode): F[VisaRequirementsFor] = {
    val ifEmpty: F[VisaRequirementsFor] = Effect[F].raiseError(CountryNotFound(to))
    SBConfiguration.countryName(to).fold(ifEmpty) { countryName =>
      wikiPageParser.visaRequirementsFor(from, countryName)
    }
  }

}