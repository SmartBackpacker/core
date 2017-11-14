package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import shapeless._

package object persistence {

  type AirlineDTO   = Int :: String :: Int :: Option[String] :: Option[String] :: HNil
  type AllowanceDTO = String :: Option[Int] :: Int :: Int :: Int :: HNil

  implicit class BaggageAllowanceConversions(allowance: AllowanceDTO) {
    def toBaggageAllowance: BaggageAllowance =
      BaggageAllowance(
        baggageType = BaggageType.fromString(allowance.head).orNull,
        kgs = allowance(1),
        size = BaggageSize(allowance(2), allowance(3), allowance(4))
      )
  }

  implicit class AirlineConversions(airline: AirlineDTO) {
    def toAirline(b: List[AllowanceDTO]): Airline =
      Airline(
        name = airline(1).as[AirlineName],
        baggagePolicy = BaggagePolicy(
          allowance = b.map(_.toBaggageAllowance),
          extra = airline(3),
          website = airline(4)
        )
      )
  }

}
