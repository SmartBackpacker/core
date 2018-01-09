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

package com.smartbackpackerapp

import com.smartbackpackerapp.model.{AirlineName, CountryCode, Currency}

package object service {

  case class CurrencyExchangeDTO(base: String, date: String, rates: Map[String, Double])

  object CurrencyExchangeDTO {
    def empty(baseCurrency: Currency): CurrencyExchangeDTO = CurrencyExchangeDTO(
      baseCurrency.value, "",
      Map(baseCurrency.value -> 0.0)
    )
  }

  sealed trait ValidationError extends Exception
  case object CountriesMustBeDifferent extends ValidationError
  case class CountryNotFound(countryCode: CountryCode) extends ValidationError
  case class AirlineNotFound(airlineName: AirlineName) extends ValidationError
  case class VisaRestrictionsIndexNotFound(countryCode: CountryCode) extends ValidationError
  case class HealthInfoNotFound(countryCode: CountryCode) extends ValidationError

}
