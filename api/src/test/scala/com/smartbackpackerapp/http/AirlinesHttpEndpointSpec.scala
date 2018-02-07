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

import cats.effect.IO
import com.smartbackpackerapp.common.IOAssertion
import com.smartbackpackerapp.http.Http4sUtils._
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.repository.algebra.AirlineRepository
import com.smartbackpackerapp.service.AirlineService
import org.http4s.{HttpService, Query, Request, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class AirlinesHttpEndpointSpec extends FlatSpecLike with Matchers with AirlinesHttpEndpointFixture {

  forAll(examples) { (airline, expectedStatus, expectedBody) =>
    it should s"find the airline $airline" in IOAssertion {
      val request = Request[IO](uri = Uri(path = s"/$ApiVersion/airlines", query = Query(("name", Some(airline)))))

      httpService(request).value.map { task =>
        task.fold(fail("Empty response")){ response =>
          response.status should be (expectedStatus)
          assert(response.body.asString.contains(expectedBody))
        }
      }
    }
  }

}

trait AirlinesHttpEndpointFixture extends PropertyChecks {

  private val airlines: List[Airline] = List(
    Airline(AirlineName("Aer Lingus"), BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 24)),
        BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
      ),
      extra = None,
      website = Some("https://www.aerlingus.com/travel-information/baggage-information/cabin-baggage/"))
    ),
    Airline(AirlineName("Transavia"), BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 25))
      ),
      extra = None,
      website = Some("https://www.transavia.com/en-EU/service/hand-luggage/"))
    )
  )

  private val testAirlineRepo = new AirlineRepository[IO] {
    override def findAirline(airlineName: AirlineName): IO[Option[Airline]] = IO {
      airlines.find(_.name.value == airlineName.value)
    }
  }

  private implicit val errorHandler = new HttpErrorHandler[IO]

  val httpService: HttpService[IO] =
    ioMiddleware(
      new AirlinesHttpEndpoint(
        new AirlineService[IO](testAirlineRepo)
      ).service
    )

  val examples = Table(
    ("airline", "expectedStatus", "expectedBody"),
    ("Aer Lingus", Status.Ok, "baggagePolicy"),
    ("Transavia", Status.Ok, "baggagePolicy"),
    ("Ryan Air", Status.NotFound, """{"code":"100","error":"Airline not found Ryan Air"}""")
  )

}
