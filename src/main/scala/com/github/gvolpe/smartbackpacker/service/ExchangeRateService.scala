package com.github.gvolpe.smartbackpacker.service

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.model.Currency
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.circe._
import org.http4s.client.Client

object ExchangeRateService {
  def apply[F[_] : Effect]: ExchangeRateService[F] = new ExchangeRateService[F](PooledHttp1Client[F]())
}

class ExchangeRateService[F[_] : Effect](client: Client[F]) extends AbstractExchangeRateService[F] {

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  override protected def retrieveExchangeRate(uri: String): F[CurrencyExchangeDTO] = {
    client.expect[CurrencyExchangeDTO](uri)
  }

}

abstract class AbstractExchangeRateService[F[_] : Effect] {

  protected val fixerUri: Currency => Currency => String = baseCurrency => foreignCurrency => {
    s"http://api.fixer.io/latest?base=$baseCurrency&symbols=$foreignCurrency"
  }

  protected def retrieveExchangeRate(uri: String): F[CurrencyExchangeDTO]

  def exchangeRateFor(baseCurrency: Currency, foreignCurrency: Currency): F[CurrencyExchangeDTO] = {
    val ifEmpty = Effect[F].pure(CurrencyExchangeDTO(baseCurrency, "", Map(baseCurrency -> 0.0)))
    validateCurrencies(baseCurrency, foreignCurrency).fold(ifEmpty) { uri =>
      Effect[F].map(retrieveExchangeRate(uri)) { exchangeRate =>
        if (exchangeRate.rates.nonEmpty) exchangeRate
        else exchangeRate.copy(rates = Map(baseCurrency -> -1.0))
      }
    }
  }

  private def validateCurrencies(baseCurrency: Currency, foreignCurrency: Currency): Option[String] = {
    if (baseCurrency == foreignCurrency) None
    else Some(fixerUri(baseCurrency)(foreignCurrency))
  }

}
