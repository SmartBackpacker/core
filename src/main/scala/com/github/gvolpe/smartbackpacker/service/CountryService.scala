package com.github.gvolpe.smartbackpacker.service

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model.{CountryCode, Currency, DestinationInfo, ExchangeRate, VisaRequirements, VisaRequirementsFor}
import com.github.gvolpe.smartbackpacker.parser.{AbstractWikiPageParser, WikiPageParser}
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.circe._
import org.http4s.client.Client

object CountryService {
  def apply[F[_] : Effect]: CountryService[F] = new CountryService[F](PooledHttp1Client[F](), WikiPageParser[F])
}

class CountryService[F[_] : Effect](httpClient: Client[F], wikiPageParser: AbstractWikiPageParser[F]) {

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  def destinationInformation(from: CountryCode, to: CountryCode, baseCurrency: Currency): F[DestinationInfo] = {
    import cats.syntax.apply._

    Effect[F].flatMap(validateCountries(from, to)) { _ =>
      val foreignCurrency = SBConfiguration.countryCurrency(to).getOrElse("EUR")

      (visaRequirementsFor(from, to), exchangeRateFor(baseCurrency, foreignCurrency)).mapN { (vr, er) =>
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

  private def exchangeRateFor(baseCurrency: Currency, foreignCurrency: Currency): F[CurrencyExchangeDTO] = {
    val fixerUri = s"http://api.fixer.io/latest?base=$baseCurrency&symbols=$foreignCurrency"
    if (baseCurrency != foreignCurrency) httpClient.expect[CurrencyExchangeDTO](fixerUri)
    else Effect[F].delay(CurrencyExchangeDTO(baseCurrency, "", Map.empty))
  }

  private def visaRequirementsFor(from: CountryCode, to: CountryCode): F[VisaRequirementsFor] = {
    val countryName = SBConfiguration.countryName(to).getOrElse("")
    WikiPageParser[F].visaRequirementsFor(from, countryName)
  }

}