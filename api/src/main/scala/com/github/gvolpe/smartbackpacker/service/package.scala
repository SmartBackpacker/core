package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model.{AirlineName, CountryCode}

package object service {

  case class CurrencyExchangeDTO(base: String, date: String, rates: Map[String, Double])

  sealed trait ValidationError extends Exception
  case object CountriesMustBeDifferent extends ValidationError
  case class CountryNotFound(countryCode: CountryCode) extends ValidationError
  case class AirlineNotFound(airlineName: AirlineName) extends ValidationError
  case class VisaRestrictionsIndexNotFound(countryCode: CountryCode) extends ValidationError
  case class HealthInfoNotFound(countryCode: CountryCode) extends ValidationError

}
