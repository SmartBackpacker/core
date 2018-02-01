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
import com.smartbackpackerapp.model.{Count, CountryCode, Ranking, Sharing, VisaRestrictionsIndex}
import com.smartbackpackerapp.repository.algebra.VisaRestrictionsIndexRepository
import org.scalatest.{FlatSpecLike, Matchers}

class VisaRestrictionsIndexServiceSpec extends FlatSpecLike with Matchers {

  private val testIndex = VisaRestrictionsIndex(Ranking(3), Count(2), Sharing(1))

  private val repo = new VisaRestrictionsIndexRepository[IO] {
    override def findRestrictionsIndex(countryCode: CountryCode): IO[Option[VisaRestrictionsIndex]] = IO {
      if (countryCode.value == "AR") Some(testIndex)
      else None
    }
  }

  private val service = new VisaRestrictionIndexService[IO](repo)

  it should "find the visa restrictions index" in IOAssertion {
    EitherT(service.findIndex(CountryCode("AR"))).map { index =>
      index should be (testIndex)
    }.value
  }

  it should "NOT find the visa restrictions index" in IOAssertion {
    EitherT(service.findIndex(CountryCode("XX"))).leftMap { error =>
      error shouldBe a [VisaRestrictionsIndexNotFound]
    }.value
  }

}
