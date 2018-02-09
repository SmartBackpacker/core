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

import cats.effect.IO
import com.smartbackpackerapp.common.IOAssertion
import com.smartbackpackerapp.config.SBConfiguration
import com.smartbackpackerapp.model.Currency
import org.scalatest.{FlatSpecLike, Matchers}

class ExchangeServiceSpec extends FlatSpecLike with Matchers {

  private lazy val sbConfig = new SBConfiguration[IO]

  private val service  = new AbstractExchangeRateService[IO](sbConfig) {
    override protected def retrieveExchangeRate(uri: String): IO[CurrencyExchangeDTO] = IO {
      CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59))
    }
  }

  it should "retrieve a fake exchange rate" in IOAssertion {
    service.exchangeRateFor(Currency("EUR"), Currency("RON")).map { exchangeRate =>
      exchangeRate should be(CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59)))
    }
  }

  it should "return an empty exchange rate" in IOAssertion {
    service.exchangeRateFor(Currency(""), Currency("")).map { exchangeRate =>
      exchangeRate should be(CurrencyExchangeDTO("", "", Map("" -> 0.0)))
    }
  }

}
