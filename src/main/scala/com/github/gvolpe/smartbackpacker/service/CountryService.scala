package com.github.gvolpe.smartbackpacker.service

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.model.{DestinationInfo, ExchangeRate}
import com.github.gvolpe.smartbackpacker.parser.WikiPageParser
import io.circe.Decoder
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.circe._

object DefaultCountryService extends App with CountryService

trait CountryService {

  val baseCurrency    = "EUR"
  val baseCountryCode = "AR"

  val countryCode = "BG"
  val countryInfoUri = s"https://restcountries.eu/rest/v2/alpha/$countryCode" // ?fields=timezones;currencies;languages

  val fixerUri = s"http://api.fixer.io/latest?base=$baseCurrency&symbols="

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[IO, A] = jsonOf[IO, A]

  case class LocalCurrency(code: String,
                           name: String,
                           symbol: String)

  case class LocalLanguage(name: String)

  case class CountryInfo(name: String,
                         timezones: List[String],
                         currencies: List[LocalCurrency],
                         languages: List[LocalLanguage])

//  case class Rate(currencyName: String, rate: Double)
  case class Rate(BGN: Double)
  case class CurrencyExchange(base: String, date: String, rates: Rate)

  val httpClient = PooledHttp1Client[IO]()
//  val result: IO[CountryInfo] = httpClient.expect[CountryInfo](countryInfoUri)

  val result = for {
    countryInfo       <- httpClient.expect[CountryInfo](countryInfoUri)
    exchangeRate      <- httpClient.expect[CurrencyExchange](fixerUri + countryInfo.currencies.head.code)
    visaRequirements  <- IO { WikiPageParser.visaRequirementsFor(baseCountryCode, countryInfo.name) }
  } yield DestinationInfo(
    countryName = countryInfo.name,
    countryCode = countryCode,
    visaRequirements = visaRequirements,
    exchangeRate = ExchangeRate(exchangeRate.base, exchangeRate.date, exchangeRate.rates),
    language = countryInfo.languages.head.name,
    timezone = countryInfo.timezones.head
  )

  println(result.unsafeRunSync())

}
