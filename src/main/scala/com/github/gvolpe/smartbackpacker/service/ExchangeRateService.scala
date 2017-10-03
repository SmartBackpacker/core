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
    if (baseCurrency != foreignCurrency) retrieveExchangeRate(fixerUri(baseCurrency)(foreignCurrency))
    else Effect[F].delay(CurrencyExchangeDTO(baseCurrency, "", Map.empty))
  }

}
