package com.github.gvolpe.smartbackpacker.service

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.model.{CountryCode, Currency, DestinationInfo, ExchangeRate}
import com.github.gvolpe.smartbackpacker.parser.WikiPageParser
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.circe._

//TODO: Try to make it generic using cats.effect.Effect
object CountryService {
  def apply(): CountryService = new CountryService()
  ///def apply[F[_] : Effect]: CountryService[F] = new CountryService[F]()
}

class CountryService() {

  val httpClient = PooledHttp1Client[IO]()

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[IO, A] = jsonOf[IO, A]
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[IO, A] = jsonEncoderOf[IO, A]

  case class LocalCurrencyDTO(code: String,
                              name: String,
                              symbol: String)

  case class LocalLanguageDTO(name: String)

  case class CountryInfoDTO(name: String,
                            timezones: List[String],
                            currencies: List[LocalCurrencyDTO],
                            languages: List[LocalLanguageDTO])

  case class CurrencyExchangeDTO(base: String, date: String, rates: Map[String, Double])

  // TODO: Perform parallel calls whenever possible to improve throughput
  def destinationInformation(from: CountryCode, to: CountryCode, baseCurrency: Currency): IO[DestinationInfo] = {
    val countryInfoUri = s"https://restcountries.eu/rest/v2/alpha/$to" // ?fields=timezones;currencies;languages
    val fixerUri = s"http://api.fixer.io/latest?base=$baseCurrency&symbols="

    for {
      countryInfo       <- httpClient.expect[CountryInfoDTO](countryInfoUri)
      foreignCurrency   = countryInfo.currencies.head.code
      exchangeRate      <- httpClient.expect[CurrencyExchangeDTO](fixerUri + foreignCurrency)
      parsedRate        = exchangeRate.rates.getOrElse(foreignCurrency, 0.0)
      visaRequirements  <- WikiPageParser[IO].visaRequirementsFor(from, countryInfo.name)
    } yield DestinationInfo(
      countryName = countryInfo.name,
      countryCode = to,
      visaRequirements = visaRequirements,
      exchangeRate = ExchangeRate(exchangeRate.base, foreignCurrency, exchangeRate.date, parsedRate),
      language = countryInfo.languages.head.name,
      timezone = countryInfo.timezones.head
    )
  }

}
