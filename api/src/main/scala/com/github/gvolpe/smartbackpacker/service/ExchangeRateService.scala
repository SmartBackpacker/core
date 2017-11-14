package com.github.gvolpe.smartbackpacker.service

import cats.effect.Effect
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model.Currency
import io.circe.generic.auto._
import org.http4s.client.blaze._
import org.http4s.circe._
import org.http4s.client.{Client, UnexpectedStatus}

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
    val uri = SBConfiguration.fixerBaseUri.getOrElse("http://localhost:8081")
    s"$uri/latest?base=${baseCurrency.value}&symbols=${foreignCurrency.value}"
  }

  protected def retrieveExchangeRate(uri: String): F[CurrencyExchangeDTO]

  def exchangeRateFor(baseCurrency: Currency, foreignCurrency: Currency): F[CurrencyExchangeDTO] = {
    val ifEmpty = CurrencyExchangeDTO(baseCurrency.value, "", Map(baseCurrency.value -> 0.0)).pure
    validateCurrencies(baseCurrency, foreignCurrency).fold(ifEmpty) { uri =>
      retrieveExchangeRate(uri).map { exchangeRate =>
        if (exchangeRate.rates.nonEmpty) exchangeRate
        else exchangeRate.copy(rates = Map(baseCurrency.value -> -1.0))
      }.recoverWith {
        case _: UnexpectedStatus => ifEmpty
      }
    }
  }

  private def validateCurrencies(baseCurrency: Currency, foreignCurrency: Currency): Option[String] = {
    if (baseCurrency == foreignCurrency) None
    else Some(fixerUri(baseCurrency)(foreignCurrency))
  }

}
