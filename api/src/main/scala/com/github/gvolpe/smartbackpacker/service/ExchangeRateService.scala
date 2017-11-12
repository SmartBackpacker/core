package com.github.gvolpe.smartbackpacker.service

import cats.{Applicative, Functor}
import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.model.Currency
import io.circe.generic.auto._
import org.http4s.client.blaze._
import org.http4s.circe._
import org.http4s.client.Client

object ExchangeRateService {
  def apply[F[_] : Effect]: ExchangeRateService[F] = new ExchangeRateService[F](PooledHttp1Client[F]())
}

class ExchangeRateService[F[_] : Effect](client: Client[F]) extends AbstractExchangeRateService[F] {

  override protected def retrieveExchangeRate(uri: String): F[CurrencyExchangeDTO] = {
    client.expect[CurrencyExchangeDTO](uri)(jsonOf[F, CurrencyExchangeDTO])
  }

}

abstract class AbstractExchangeRateService[F[_] : Effect] {

  protected val fixerUri: Currency => Currency => String = baseCurrency => foreignCurrency => {
    //s"http://api.fixer.io/latest?base=${baseCurrency.value}&symbols=${foreignCurrency.value}"
    s"http://localhost:8081/latest?base=${baseCurrency.value}&symbols=${foreignCurrency.value}"
  }

  protected def retrieveExchangeRate(uri: String): F[CurrencyExchangeDTO]

  def exchangeRateFor(baseCurrency: Currency, foreignCurrency: Currency): F[CurrencyExchangeDTO] = {
    val ifEmpty = Applicative[F].pure(CurrencyExchangeDTO(baseCurrency.value, "", Map(baseCurrency.value -> 0.0)))
    validateCurrencies(baseCurrency, foreignCurrency).fold(ifEmpty) { uri =>
      Functor[F].map(retrieveExchangeRate(uri)) { exchangeRate =>
        if (exchangeRate.rates.nonEmpty) exchangeRate
        else exchangeRate.copy(rates = Map(baseCurrency.value -> -1.0))
      }
    }
  }

  private def validateCurrencies(baseCurrency: Currency, foreignCurrency: Currency): Option[String] = {
    if (baseCurrency == foreignCurrency) None
    else Some(fixerUri(baseCurrency)(foreignCurrency))
  }

}
