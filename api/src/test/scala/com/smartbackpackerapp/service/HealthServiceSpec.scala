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
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.repository.algebra.HealthRepository
import org.scalatest.{FlatSpecLike, Matchers}

class HealthServiceSpec extends FlatSpecLike with Matchers {

  private val testHealth = Health(
    vaccinations = Vaccinations(List.empty[Vaccine], List.empty[Vaccine], List.empty[Vaccine]),
    notices = HealthNotices(
      alertLevel = LevelOne,
      alerts = List.empty[HealthAlert]
    )
  )

  private val repo = new HealthRepository[IO] {
    override def findHealthInfo(countryCode: CountryCode): IO[Option[Health]] = IO {
      if (countryCode.value == "AR") Some(testHealth)
      else None
    }
  }

  private val service = new HealthService[IO](repo)

  it should "find the health information" in IOAssertion {
    EitherT(service.findHealthInfo(CountryCode("AR"))).map { index =>
      index should be (testHealth)
    }.value
  }

  it should "NOT find the health information" in IOAssertion {
    EitherT(service.findHealthInfo(CountryCode("XX"))).leftMap { error =>
      error shouldBe a [HealthInfoNotFound]
    }.value
  }

}
