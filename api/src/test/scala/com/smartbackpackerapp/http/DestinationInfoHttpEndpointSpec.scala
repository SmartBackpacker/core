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

package com.smartbackpackerapp.http

import cats.Parallel
import cats.syntax.option._
import com.smartbackpackerapp.common.TaskAssertion
import com.smartbackpackerapp.config.SBConfiguration
import com.smartbackpackerapp.http.Http4sUtils._
import com.smartbackpackerapp.model.{Country, CountryCode, CountryName, Currency, VisaNotRequired, VisaRequirementsData}
import com.smartbackpackerapp.repository.algebra.VisaRequirementsRepository
import com.smartbackpackerapp.service.{AbstractExchangeRateService, CurrencyExchangeDTO, DestinationInfoService}
import monix.eval.Task
import org.http4s.{HttpService, Query, Request, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class DestinationInfoHttpEndpointSpec extends FlatSpecLike with Matchers with DestinationInfoHttpEndpointFixture {

  forAll(examples) { (from, to, expectedStatus, expectedCountry, expectedVisa) =>
    it should s"retrieve visa requirements from $from to $to" in TaskAssertion {
      val request = Request[Task](uri = Uri(path = s"/$ApiVersion/traveling/$from/to/$to", query = Query(("baseCurrency", Some("EUR")))))

      httpService(request).value.map { task =>
        task.fold(fail("Empty response")){ response =>
          response.status should be (expectedStatus)

          val body = response.body.asString
          assert(body.contains(expectedCountry))
          assert(body.contains(expectedVisa))
        }
      }
    }
  }

}

trait DestinationInfoHttpEndpointFixture extends PropertyChecks {

  val examples = Table(
    ("from", "code", "expectedStatus","expectedCountry", "expectedVisa"),
    ("AR", "GB", Status.Ok, "United Kingdom", "VisaNotRequired"),
    ("AR", "KO", Status.NotFound, "Country not found", """{"code":"100","error":"Country not found KO"}"""),
    ("AR", "AR", Status.BadRequest, "Countries must be different", """{"code":"101","error":"Countries must be different!"}""")
  )

  private val repo = new VisaRequirementsRepository[Task] {
    override def findVisaRequirements(from: CountryCode, to: CountryCode): Task[Option[VisaRequirementsData]] = Task {
      if (to.value == "KO") none[VisaRequirementsData]
      else
      VisaRequirementsData(
        from = Country(CountryCode("AR"), CountryName("Argentina"), Currency("ARS")),
        to   = Country(CountryCode("GB"), CountryName("United Kingdom"), Currency("GBP")),
        visaCategory = VisaNotRequired,
        description = "90 days within any 180 day period"
      ).some
    }
  }

  private lazy val sbConfig = new SBConfiguration[Task]

  private val rateService = new AbstractExchangeRateService[Task](sbConfig) {
    override protected def retrieveExchangeRate(uri: String): Task[CurrencyExchangeDTO] = Task {
      CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59))
    }
  }

  private implicit val errorHandler = new HttpErrorHandler[Task]

  private implicit val parallel: Parallel[Task, Task] =
    Parallel[Task, Task.Par].asInstanceOf[Parallel[Task, Task]]

  val httpService: HttpService[Task] =
    taskMiddleware(
      new DestinationInfoHttpEndpoint(
        new DestinationInfoService[Task](sbConfig, repo, rateService)
      ).service
    )

}
