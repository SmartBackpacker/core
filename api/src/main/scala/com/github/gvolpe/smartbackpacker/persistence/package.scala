package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import shapeless._

package object persistence {

  type AirlineDTO           = Int :: String :: Int :: Option[String] :: Option[String] :: HNil
  type BaggageAllowanceDTO  = String :: Option[Int] :: Int :: Int :: Int :: HNil
  type RestrictionsIndexDTO = Int :: Int :: Int :: HNil

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
        rank = index.head,
        count = index.tail.head,
        sharing = index.last
      )
  }

}
