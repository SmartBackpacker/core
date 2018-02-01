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

import cats.data.EitherT
import cats.effect.IO
import com.smartbackpackerapp.common.IOAssertion
import com.smartbackpackerapp.model.{Airline, AirlineName, BaggageAllowance, BaggagePolicy}
import com.smartbackpackerapp.repository.algebra.AirlineRepository
import org.scalatest.{FlatSpecLike, Matchers}

class AirlineServiceSpec extends FlatSpecLike with Matchers {

  private val testAirline = Airline(
    name = AirlineName("Ryan Air"),
    baggagePolicy = BaggagePolicy(
      allowance = List.empty[BaggageAllowance],
      extra = None,
      website = None
    )
  )

  private val repo = new AirlineRepository[IO] {
    override def findAirline(airlineName: AirlineName): IO[Option[Airline]] = IO {
      if (airlineName.value == "Ryan Air") Some(testAirline)
      else None
    }
  }

  private val service  = new AirlineService[IO](repo)

  it should "find the airline" in IOAssertion {
    EitherT(service.baggagePolicy(AirlineName("Ryan Air"))).map { airline =>
      airline should be (testAirline)
    }.value
  }

  it should "NOT find the airline" in IOAssertion {
    EitherT(service.baggagePolicy(AirlineName("Futur Airways"))).leftMap { error =>
      error shouldBe a [AirlineNotFound]
    }.value
  }

}
