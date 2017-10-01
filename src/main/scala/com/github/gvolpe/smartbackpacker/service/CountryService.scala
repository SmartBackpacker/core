package com.github.gvolpe.smartbackpacker.service

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model.{CountryCode, Currency, DestinationInfo, ExchangeRate, VisaRequirements, VisaRequirementsFor}
import com.github.gvolpe.smartbackpacker.parser.WikiPageParser
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.circe._
import org.http4s.client.Client

//TODO: Try to make it generic using cats.effect.Effect
object CountryService {
  def apply(): CountryService = new CountryService()
  ///def apply[F[_] : Effect]: CountryService[F] = new CountryService[F]()
}

class CountryService() {

  val httpClient: Client[IO] = PooledHttp1Client[IO]()

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[IO, A] = jsonOf[IO, A]
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[IO, A] = jsonEncoderOf[IO, A]

  case class LocalCurrencyDTO(code: String,
                              name: String,
                              symbol: String)

  case class LocalLanguageDTO(name: String)

  case class CurrencyExchangeDTO(base: String, date: String, rates: Map[String, Double])

  object CountriesMustBeDifferent extends Exception("Countries must be different!")

  def destinationInformation(from: CountryCode, to: CountryCode, baseCurrency: Currency): IO[DestinationInfo] = {
    import cats.syntax.apply._

    validateCountries(from, to).flatMap { _ =>
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

  private def validateCountries(from: CountryCode, to: CountryCode): IO[(CountryCode, CountryCode)] = {
    if (from != to) IO((from, to))
    else IO.raiseError(CountriesMustBeDifferent)
  }

  private def exchangeRateFor(baseCurrency: Currency, foreignCurrency: Currency): IO[CurrencyExchangeDTO] = {
    val fixerUri = s"http://api.fixer.io/latest?base=$baseCurrency&symbols=$foreignCurrency"
    if (baseCurrency != foreignCurrency) httpClient.expect[CurrencyExchangeDTO](fixerUri)
    else IO(CurrencyExchangeDTO(baseCurrency, "", Map.empty))
  }

  private def visaRequirementsFor(from: CountryCode, to: CountryCode): IO[VisaRequirementsFor] = {
    val countryName = SBConfiguration.countryName(to).getOrElse("")
    WikiPageParser[IO].visaRequirementsFor(from, countryName)
  }

}
