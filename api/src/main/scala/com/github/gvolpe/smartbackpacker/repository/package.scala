package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import shapeless._

package object repository {

  type AirlineDTO           = Int :: String :: Int :: Option[String] :: Option[String] :: HNil
  type BaggageAllowanceDTO  = String :: Option[Int] :: Int :: Int :: Int :: HNil
  type RestrictionsIndexDTO = Int :: Int :: Int :: HNil

  type CountryDTO           = Int :: String :: String :: HNil
  type VisaRequirementsDTO  = String :: String :: HNil

  type VaccineDTO           = String :: String :: String :: HNil
  type HealthNoticeDTO      = String :: String :: String :: HNil
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
        name = airline(1).as[AirlineName],
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
        rank = new Ranking(index.head),
        count = new Count(index.tail.head),
        sharing = new Sharing(index.last)
      )
  }

  implicit class VisaRequirementsConversions(dto: VisaRequirementsDTO) {
    def toVisaRequirementsData(fromDto: CountryDTO, toDto: CountryDTO): VisaRequirementsData =
      VisaRequirementsData(
        from          = Country(new CountryCode(fromDto(1)), new CountryName(fromDto.last)),
        to            = Country(new CountryCode(toDto(1)), new CountryName(toDto.last)),
        visaCategory  = VisaCategory.fromName(dto.head),
        description   = dto.last
      )
  }

  implicit class VaccineConversions(dto: VaccineDTO) {
    private def parseDiseaseCategories(value: String): List[DiseaseCategory] = {
      value.split(',').toList.map(DiseaseCategory.fromString)
    }

    def toVaccine: Vaccine =
      Vaccine(
        disease = dto.head.as[Disease],
        description = dto.tail.head,
        diseaseCategories = parseDiseaseCategories(dto.last)
      )
  }

  implicit class HealthNoticeConversions(dto: HealthNoticeDTO) {
    def toHealthAlert(alertLevel: AlertLevel): HealthAlert =
      HealthAlert(
        title = dto.head,
        link = dto.tail.head.as[WebLink],
        description = dto.last
      )
  }

  implicit class HealthAlertConversions(dto: HealthAlertDTO) {
    def toAlertLevel: AlertLevel = AlertLevel.fromString(dto.head)
  }

}
