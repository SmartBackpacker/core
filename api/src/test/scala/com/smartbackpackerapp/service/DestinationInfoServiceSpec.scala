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

import cats.Parallel
import cats.data.EitherT
import cats.syntax.option._
import com.smartbackpackerapp.common.TaskAssertion
import com.smartbackpackerapp.config.SBConfiguration
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.repository.algebra.VisaRequirementsRepository
import monix.eval.Task
import org.scalatest.{FlatSpecLike, Matchers}

class DestinationInfoServiceSpec extends FlatSpecLike with Matchers {

  private lazy val sbConfig = new SBConfiguration[Task]

  private val repo = new VisaRequirementsRepository[Task] {
    override def findVisaRequirements(from: CountryCode, to: CountryCode): Task[Option[VisaRequirementsData]] = Task {
      VisaRequirementsData(
        from = Country(CountryCode("AR"), CountryName("Argentina"), Currency("ARS")),
        to   = Country(CountryCode("RO"), CountryName("Romania"), Currency("RON")),
        visaCategory = VisaNotRequired,
        description = "90 days within any 180 day period"
      ).some
    }
  }

  private val rateService = new AbstractExchangeRateService[Task](sbConfig) {
    override protected def retrieveExchangeRate(uri: String): Task[CurrencyExchangeDTO] = Task {
      CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59))
    }
  }

  private implicit val parallel: Parallel[Task, Task] =
    Parallel[Task, Task.Par].asInstanceOf[Parallel[Task, Task]]

  private val service = new DestinationInfoService[Task](sbConfig, repo, rateService)

  it should "retrieve destination information" in TaskAssertion {
    EitherT(service.find(CountryCode("AR"), CountryCode("RO"), Currency("EUR"))).map { info =>
      info.countryCode.value  should be ("RO")
      info.countryName.value  should be ("Romania")
      info.exchangeRate       should be (ExchangeRate(Currency("EUR"), Currency("RON"), 4.59))
      info.visaRequirements   should be (VisaRequirements(VisaNotRequired, "90 days within any 180 day period"))
    }.value
  }

  it should "validate countries" in TaskAssertion {
    EitherT(service.find(CountryCode("AR"), CountryCode("AR"), Currency("EUR"))).leftMap { error =>
      error should be (CountriesMustBeDifferent)
    }.value
  }

}
