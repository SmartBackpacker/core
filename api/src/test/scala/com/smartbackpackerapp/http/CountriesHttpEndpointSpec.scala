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
import com.smartbackpackerapp.model.{Country, CountryCode, CountryName, Currency}
import com.smartbackpackerapp.repository.algebra.CountryRepository
import com.smartbackpackerapp.service.CountryService
import org.http4s.{HttpService, Query, Request, Status, Uri}
import org.scalatest.{FlatSpecLike, Matchers}

class CountriesHttpEndpointSpec extends FlatSpecLike with Matchers with CountriesHttpEndpointFixture {

  it should s"find all the countries" in IOAssertion {
    val request = Request[IO](uri = Uri(path = s"/$ApiVersion/countries"))

    httpService(request).value.map { task =>
      task.fold(fail("Empty response")){ response =>
        response.status should be (Status.Ok)
        assert(response.body.asString.contains("Argentina"))
      }
    }
  }

  it should s"find all the schengen countries" in IOAssertion {
    val request = Request[IO](uri = Uri(path = s"/$ApiVersion/countries", query = Query(("query", Some("schengen")))))

    httpService(request).value.map { task =>
      task.fold(fail("Empty response")){ response =>
        response.status should be (Status.Ok)
        assert(response.body.asString.contains("Poland"))
      }
    }
  }

}

trait CountriesHttpEndpointFixture {

  private val testCountries = List(
    Country(CountryCode("AR"), CountryName("Argentina"), Currency("ARS"))
  )

  private val testSchengenCountries = List(
    Country(CountryCode("PL"), CountryName("Poland"), Currency("PLN"))
  )

  private val repo = new CountryRepository[IO] {
    override def findAll: IO[List[Country]] = IO(testCountries)
    override def findSchengen: IO[List[Country]] = IO(testSchengenCountries)
  }

  private implicit val errorHandler = new HttpErrorHandler[IO]

  val httpService: HttpService[IO] =
    ioMiddleware(
      new CountriesHttpEndpoint[IO](
        new CountryService[IO](repo)
      ).service
    )

}
