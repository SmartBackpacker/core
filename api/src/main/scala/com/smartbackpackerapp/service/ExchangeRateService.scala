/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbackpackerapp.service

import java.net.ConnectException

import cats.MonadError
import cats.effect.Effect
import cats.syntax.all._
import com.smartbackpackerapp.common.Log
import com.smartbackpackerapp.config.SBConfiguration
import com.smartbackpackerapp.model.Currency
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
