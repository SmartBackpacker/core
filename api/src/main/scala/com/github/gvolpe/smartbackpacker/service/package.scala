package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model.{AirlineName, CountryCode}

package object service {

  case class CurrencyExchangeDTO(base: String, date: String, rates: Map[String, Double])

  sealed trait ApiServiceError extends Exception
  case object CountriesMustBeDifferent extends ApiServiceError
  case class CountryNotFound(countryCode: CountryCode) extends ApiServiceError
  case class AirlineNotFound(airlineName: AirlineName) extends ApiServiceError

}
