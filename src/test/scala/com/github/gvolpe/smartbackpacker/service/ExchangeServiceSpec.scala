package com.github.gvolpe.smartbackpacker.service

import com.github.gvolpe.smartbackpacker.TestExchangeRateService
import org.scalatest.{FlatSpecLike, Matchers}

class ExchangeServiceSpec extends FlatSpecLike with Matchers {

  behavior of "ExchangeService"

  it should "retrieve a fake exchange rate" in {
    val exchangeRate = TestExchangeRateService.exchangeRateFor("EUR", "RON").unsafeRunSync()
    exchangeRate should be (CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59)))
  }

  it should "return an empty exchange rate" in {
    val exchangeRate = TestExchangeRateService.exchangeRateFor("", "").unsafeRunSync()
    exchangeRate should be (CurrencyExchangeDTO("", "", Map("" -> 0.0)))
  }

}
