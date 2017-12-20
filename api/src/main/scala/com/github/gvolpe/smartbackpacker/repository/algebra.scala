package com.github.gvolpe.smartbackpacker.repository

import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName, CountryCode, Health, VisaRequirementsData, VisaRestrictionsIndex}

object algebra {

  trait AirlineRepository[F[_]] {
    def findAirline(airlineName: AirlineName): F[Option[Airline]]
  }

  trait HealthRepository[F[_]] {
    def findHealthInfo(countryCode: CountryCode): F[Option[Health]]
  }

  trait VisaRequirementsRepository[F[_]] {
    def find(from: CountryCode, to: CountryCode): F[Option[VisaRequirementsData]]
  }

  trait VisaRestrictionsIndexRepository[F[_]] {
    def findIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]]
  }

}
