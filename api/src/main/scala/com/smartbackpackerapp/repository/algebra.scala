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

package com.smartbackpackerapp.repository

import com.smartbackpackerapp.model.{Airline, AirlineName, Country, CountryCode, Health, VisaRequirementsData, VisaRestrictionsIndex}

object algebra {

  trait AirlineRepository[F[_]] {
    def findAirline(airlineName: AirlineName): F[Option[Airline]]
  }

  trait HealthRepository[F[_]] {
    def findHealthInfo(countryCode: CountryCode): F[Option[Health]]
  }

  trait VisaRequirementsRepository[F[_]] {
    def findVisaRequirements(from: CountryCode, to: CountryCode): F[Option[VisaRequirementsData]]
  }

  trait VisaRestrictionsIndexRepository[F[_]] {
    def findRestrictionsIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]]
  }

  trait CountryRepository[F[_]] {
    def findAll: F[List[Country]]
    def findSchengen: F[List[Country]]
  }

}
