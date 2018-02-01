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

import com.smartbackpackerapp.model._
import shapeless._

package object repository {

  type AirlineDTO           = Int :: String :: Int :: Option[String] :: Option[String] :: HNil
  type BaggageAllowanceDTO  = String :: Option[Int] :: Int :: Int :: Int :: HNil
  type RestrictionsIndexDTO = Int :: Int :: Option[Int] :: HNil

  type CountryDTO           = Int :: String :: String :: String :: HNil
  type VisaRequirementsDTO  = Option[String] :: Option[String] :: HNil

  type VaccineDTO           = String :: String :: Option[String] :: HNil
  type HealthNoticeDTO      = String :: Option[String] :: Option[String] :: HNil
  type HealthAlertDTO       = String :: HNil

  implicit class BaggageAllowanceConversions(allowance: BaggageAllowanceDTO) {
    def toBaggageAllowance: BaggageAllowance =
      BaggageAllowance(
        baggageType = BaggageType.fromString(allowance.head).orNull,
        kgs = allowance(1),
        size = BaggageSize(allowance(2), allowance(3), allowance(4))
      )
  }

  implicit class AirlineConversions(airline: AirlineDTO) {
    def toAirline(b: List[BaggageAllowanceDTO]): Airline =
      Airline(
        name = AirlineName(airline(1)),
        baggagePolicy = BaggagePolicy(
          allowance = b.map(_.toBaggageAllowance),
          extra = airline(3),
          website = airline(4)
        )
      )
  }

  implicit class RestrictionsIndexConversions(index: RestrictionsIndexDTO) {
    def toVisaRestrictionsIndex: VisaRestrictionsIndex =
      VisaRestrictionsIndex(
        rank = Ranking(index.head),
        count = Count(index.tail.head),
        sharing = Sharing(index.last.getOrElse(0))
      )
  }

  implicit class VisaRequirementsConversions(dto: VisaRequirementsDTO) {
    def toVisaRequirementsData(fromDto: CountryDTO, toDto: CountryDTO): VisaRequirementsData =
      VisaRequirementsData(
        from          = fromDto.toCountry,
        to            = toDto.toCountry,
        visaCategory  = VisaCategory.fromName(dto.head.getOrElse("")),
        description   = dto.last.getOrElse("")
      )
  }

  implicit class VaccineConversions(dto: VaccineDTO) {
    private def parseDiseaseCategories(value: String): List[DiseaseCategory] = {
      value.split(',').toList.map(DiseaseCategory.fromString)
    }

    def toVaccine: Vaccine =
      Vaccine(
        disease = Disease(dto.head),
        description = dto.tail.head,
        diseaseCategories = parseDiseaseCategories(dto.last.getOrElse(""))
      )
  }

  implicit class HealthNoticeConversions(dto: HealthNoticeDTO) {
    def toHealthAlert: HealthAlert =
      HealthAlert(
        title = dto.head,
        link = WebLink(dto.tail.head.getOrElse("")),
        description = dto.last.getOrElse("")
      )
  }

  implicit class HealthAlertConversions(dto: HealthAlertDTO) {
    def toAlertLevel: AlertLevel = AlertLevel.fromString(dto.head)
  }

  implicit class CountryConversions(dto: CountryDTO) {
    def toCountry: Country =
      Country(
        code = CountryCode(dto(1)),
        name = CountryName(dto(2)),
        currency = Currency(dto.last)
      )
  }

}
