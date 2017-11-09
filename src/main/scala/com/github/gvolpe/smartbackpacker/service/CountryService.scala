package com.github.gvolpe.smartbackpacker.service

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.parser.{AbstractWikiPageParser, WikiPageParser}

object CountryService {
  def apply[F[_] : Effect]: CountryService[F] = new CountryService[F](WikiPageParser[F], ExchangeRateService[F])
}

class CountryService[F[_] : Effect](wikiPageParser: AbstractWikiPageParser[F], exchangeRateService: AbstractExchangeRateService[F]) {

  def destinationInformation(from: CountryCode, to: CountryCode, baseCurrency: Currency): F[DestinationInfo] = {
    import cats.syntax.apply._

    Effect[F].>>=(validateCountries(from, to)) { _ =>
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
  }

  private def validateCountries(from: CountryCode, to: CountryCode): F[(CountryCode, CountryCode)] = {
    if (from != to) Effect[F].pure((from, to))
    else Effect[F].raiseError(CountriesMustBeDifferent)
  }

  private def visaRequirementsFor(from: CountryCode, to: CountryCode): F[VisaRequirementsFor] = {
    val ifEmpty: F[VisaRequirementsFor] = Effect[F].raiseError(CountryNotFound(to))

    // Some country names have different ways to be spelled. Eg. ["Côte d'Ivoire", "Ivory Coast"]
    def iter(countryNames: List[String]): F[VisaRequirementsFor] = countryNames match {
      case (country :: Nil) =>
        wikiPageParser.visaRequirementsFor(from, country.as[CountryName])
      case (country :: xs)  =>
        Effect[F].>>=(wikiPageParser.visaRequirementsFor(from, country.as[CountryName])) { result =>
          if (result.visaCategory == UnknownVisaCategory) iter(xs)
          else Effect[F].delay(result)
        }
      case Nil              => ifEmpty
    }

    iter(SBConfiguration.countryNames(to))
  }

}