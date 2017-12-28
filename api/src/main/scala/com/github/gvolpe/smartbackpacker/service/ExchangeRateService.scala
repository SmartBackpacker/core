package com.github.gvolpe.smartbackpacker.service

import java.net.ConnectException

import cats.MonadError
import cats.effect.Effect
import cats.syntax.all._
import com.github.gvolpe.smartbackpacker.common.Log
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model.Currency
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.client.{Client, UnexpectedStatus}

class ExchangeRateService[F[_] : Effect](client: Client[F],
                                         sbConfig: SBConfiguration[F])
                                        (implicit L: Log[F]) extends AbstractExchangeRateService[F](sbConfig) {

  override protected def retrieveExchangeRate(uri: String): F[CurrencyExchangeDTO] = {
    client.expect[CurrencyExchangeDTO](uri)(jsonOf[F, CurrencyExchangeDTO])
  }

}

abstract class AbstractExchangeRateService[F[_]](sbConfig: SBConfiguration[F])
                                                (implicit F: MonadError[F, Throwable], L: Log[F]) {

  protected val fixerUri: Currency => Currency => F[String] = baseCurrency => foreignCurrency => {
    val uri = sbConfig.fixerBaseUri.map(_.getOrElse("http://localhost:8081"))
    uri.map(x => s"$x/latest?base=${baseCurrency.value}&symbols=${foreignCurrency.value}")
  }

  protected def retrieveExchangeRate(uri: String): F[CurrencyExchangeDTO]

  // We don't want the whole destination service to fail if the exchange rate service is unavailable
  // so the `UnexpectedStatus` and `ConnectException` errors are treated as an empty exchange rate
  def exchangeRateFor(baseCurrency: Currency, foreignCurrency: Currency): F[CurrencyExchangeDTO] = {
    val ifEmpty = CurrencyExchangeDTO.empty(baseCurrency).pure[F]

    def performRequest(uri: String): F[CurrencyExchangeDTO] =
      retrieveExchangeRate(uri).recoverWith {
        case e: ConnectException => L.error(e).flatMap(_ => ifEmpty)
        case _: UnexpectedStatus => ifEmpty
      }

    validateCurrencies(baseCurrency, foreignCurrency).fold(ifEmpty) { _ =>
      for {
        uri <- fixerUri(baseCurrency)(foreignCurrency)
        _   <- L.info(s"Retrieving currency exchange from: $uri")
        er  <- performRequest(uri)
      } yield {
        if (er.rates.nonEmpty) er
        else er.copy(rates = Map(baseCurrency.value -> -1.0))
      }
    }
  }

  private def validateCurrencies(baseCurrency: Currency, foreignCurrency: Currency): Option[Currency] = {
    if (baseCurrency == foreignCurrency) none[Currency]
    else foreignCurrency.some
  }

}
