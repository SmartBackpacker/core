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
import com.smartbackpackerapp.model.{Count, CountryCode, Ranking, Sharing, VisaRestrictionsIndex}
import com.smartbackpackerapp.repository.algebra.VisaRestrictionsIndexRepository
import com.smartbackpackerapp.service.VisaRestrictionIndexService
import org.http4s.{HttpService, Request, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class VisaRestrictionIndexHttpEndpointSpec extends FlatSpecLike with Matchers with VisaRestrictionIndexFixture {

  forAll(examples) { (countryCode, expectedStatus) =>
    it should s"try to retrieve visa restriction index for $countryCode" in IOAssertion {
      val request = Request[IO](uri = Uri(path = s"/$ApiVersion/ranking/$countryCode"))

      httpService(request).value.map { task =>
        task.fold(fail("Empty response")){ response =>
          response.status should be (expectedStatus)
        }
      }
    }
  }

}

trait VisaRestrictionIndexFixture extends PropertyChecks {

  import Http4sUtils._

  private val repo = new VisaRestrictionsIndexRepository[IO] {
    override def findRestrictionsIndex(countryCode: CountryCode): IO[Option[VisaRestrictionsIndex]] =
      IO {
        if (countryCode.value == "AR") Some(VisaRestrictionsIndex(Ranking(0), Count(0), Sharing(0)))
        else None
      }
  }

  private implicit val errorHandler = new HttpErrorHandler[IO]

  val httpService: HttpService[IO] =
    ioMiddleware(
      new VisaRestrictionIndexHttpEndpoint(
        new VisaRestrictionIndexService[IO](repo)
      ).service
    )

  val examples = Table(
    ("countryCode", "expectedStatus"),
    ("AR", Status.Ok),
    ("XX", Status.NotFound)
  )

}