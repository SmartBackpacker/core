package com.github.gvolpe.smartbackpacker.service

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import org.scalatest.{FlatSpecLike, Matchers}

class ExchangeServiceSpec extends FlatSpecLike with Matchers {

  private lazy val sbConfig = new SBConfiguration[IO]

  private val service  = new AbstractExchangeRateService[IO](sbConfig) {
    override protected def retrieveExchangeRate(uri: String): IO[CurrencyExchangeDTO] = IO {
      CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59))
    }
  }

  it should "retrieve a fake exchange rate" in IOAssertion {
    service.exchangeRateFor("EUR".as[Currency], "RON".as[Currency]).map { exchangeRate =>
      exchangeRate should be(CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59)))
    }
  }

  it should "return an empty exchange rate" in IOAssertion {
    service.exchangeRateFor("".as[Currency], "".as[Currency]).map { exchangeRate =>
      exchangeRate should be(CurrencyExchangeDTO("", "", Map("" -> 0.0)))
    }
  }

}
