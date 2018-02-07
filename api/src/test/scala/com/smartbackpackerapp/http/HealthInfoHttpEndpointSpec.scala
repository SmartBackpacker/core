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
import com.smartbackpackerapp.model.{CountryCode, Health, HealthAlert, HealthNotices, LevelOne, Vaccinations, Vaccine}
import com.smartbackpackerapp.repository.algebra.HealthRepository
import com.smartbackpackerapp.service.HealthService
import org.http4s.{HttpService, Request, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class HealthInfoHttpEndpointSpec extends FlatSpecLike with Matchers with HealthInfoFixture {

  forAll(examples) { (countryCode, expectedStatus) =>
    it should s"try to retrieve health information for $countryCode" in IOAssertion {
      val request = Request[IO](uri = Uri(path = s"/$ApiVersion/health/$countryCode"))

      httpService(request).value.map { task =>
        task.fold(fail("Empty response")){ response =>
          response.status should be (expectedStatus)
        }
      }
    }
  }

}

trait HealthInfoFixture extends PropertyChecks {

  import Http4sUtils._

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

  private implicit val errorHandler = new HttpErrorHandler[IO]

  val httpService: HttpService[IO] =
    ioMiddleware(
      new HealthInfoHttpEndpoint(
        new HealthService[IO](repo)
      ).service
    )

  val examples = Table(
    ("countryCode", "expectedStatus"),
    ("AR", Status.Ok),
    ("XX", Status.NotFound)
  )

}