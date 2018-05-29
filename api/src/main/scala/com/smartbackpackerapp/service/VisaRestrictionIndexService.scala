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

import cats.Functor
import cats.syntax.functor._
import com.smartbackpackerapp.model.{CountryCode, VisaRestrictionsIndex}
import com.smartbackpackerapp.repository.algebra.VisaRestrictionsIndexRepository

class VisaRestrictionIndexService[F[_] : Functor](visaRestrictionsIndexRepo: VisaRestrictionsIndexRepository[F]) {

  def findIndex(countryCode: CountryCode): F[ValidationError Either VisaRestrictionsIndex] =
    visaRestrictionsIndexRepo.findRestrictionsIndex(countryCode).map { index =>
      index.toRight[ValidationError](VisaRestrictionsIndexNotFound(countryCode))
    }

}
