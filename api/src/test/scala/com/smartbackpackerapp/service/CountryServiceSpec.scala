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
import com.smartbackpackerapp.model.{Country, CountryCode, CountryName, Currency}
import com.smartbackpackerapp.repository.algebra.CountryRepository
import org.scalatest.{FlatSpecLike, Matchers}

class CountryServiceSpec extends FlatSpecLike with Matchers {

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

  private val service = new CountryService[IO](repo)

  it should "find all the countries" in IOAssertion {
    service.findAll(false).map { countries =>
      countries should be (testCountries)
    }
  }

  it should "find all schengen the countries" in IOAssertion {
    service.findAll(true).map { countries =>
      countries should be (testSchengenCountries)
    }
  }

}
