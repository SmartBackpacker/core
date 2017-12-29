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

package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model.{Count, CountryCode, Ranking, Sharing, VisaRestrictionsIndex}
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRestrictionsIndexRepository
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
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
        if (countryCode.value == "AR") Some(VisaRestrictionsIndex(new Ranking(0), new Count(0), new Sharing(0)))
        else None
      }
  }

  val httpService: HttpService[IO] =
    middleware(
      new VisaRestrictionIndexHttpEndpoint(
        new VisaRestrictionIndexService[IO](repo),
        new HttpErrorHandler[IO]
      ).service
    )

  val examples = Table(
    ("countryCode", "expectedStatus"),
    ("AR", Status.Ok),
    ("XX", Status.NotFound)
  )

}