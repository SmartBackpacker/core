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

package com.github.gvolpe.smartbackpacker.service

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.option._
import com.github.gvolpe.smartbackpacker.common.instances.log._
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRequirementsRepository
import org.scalatest.{FlatSpecLike, Matchers}

class DestinationInfoServiceSpec extends FlatSpecLike with Matchers {

  private lazy val sbConfig = new SBConfiguration[IO]

  private val repo = new VisaRequirementsRepository[IO] {
    override def findVisaRequirements(from: CountryCode, to: CountryCode): IO[Option[VisaRequirementsData]] = IO {
      VisaRequirementsData(
        from = Country("AR".as[CountryCode], "Argentina".as[CountryName], "ARS".as[Currency]),
        to   = Country("RO".as[CountryCode], "Romania".as[CountryName], "RON".as[Currency]),
        visaCategory = VisaNotRequired,
        description = "90 days within any 180 day period"
      ).some
    }
  }

  private val rateService = new AbstractExchangeRateService[IO](sbConfig) {
    override protected def retrieveExchangeRate(uri: String): IO[CurrencyExchangeDTO] = IO {
      CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59))
    }
  }

  private val service = new DestinationInfoService[IO](sbConfig, repo, rateService)

  it should "retrieve destination information" in IOAssertion {
    EitherT(service.find("AR".as[CountryCode], "RO".as[CountryCode], "EUR".as[Currency])).map { info =>
      info.countryCode.value  should be ("RO")
      info.countryName.value  should be ("Romania")
      info.exchangeRate       should be (ExchangeRate("EUR".as[Currency], "RON".as[Currency], 4.59))
      info.visaRequirements   should be (VisaRequirements(VisaNotRequired, "90 days within any 180 day period"))
    }.value
  }

  it should "validate countries" in IOAssertion {
    EitherT(service.find("AR".as[CountryCode], "AR".as[CountryCode], "EUR".as[Currency])).leftMap { error =>
      error should be (CountriesMustBeDifferent)
    }.value
  }

}
