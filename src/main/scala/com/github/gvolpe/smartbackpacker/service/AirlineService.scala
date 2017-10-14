package com.github.gvolpe.smartbackpacker.service

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.model._

object AirlineService {
  def apply[F[_] : Effect]: AirlineService[F] = new AirlineService[F]()
}

class AirlineService[F[_] : Effect] extends AbstractAirlineService[F]

abstract class AbstractAirlineService[F[_] : Effect] {

  /**
    * https://wikitravel.org/en/Discount_airlines_in_Europe
    *
    * https://www.airberlin.com/en/site/landingpages/baggage_services.php
    * https://www.aircanada.com/ca/en/aco/home/plan/baggage/carry-on.html
    * https://www.aerlingus.com/travel-information/baggage-information/cabin-baggage/
    * http://www.easyjet.com/en/help/baggage/cabin-bag-and-hold-luggage
    * https://www.ryanair.com/gb/en/plan-trip/flying-with-us/baggage-policy
    * https://www.transavia.com/en-EU/service/hand-luggage/
    * https://www.united.com/CMS/en-US/travel/Pages/BaggageCarry-On.aspx
    * https://wizzair.com/en-gb/information-and-services/travel-information/baggage#after-october
    * */

  private val airlines = List(
    Airline("Air Berlin", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 23)),
        BaggageAllowance(SmallBag, Some(2), BaggageSize(40, 30, 10))
      ),
      extra = Some("The number of free checked baggage items varies according to the fare booked."))
    ),
    Airline("Air Canada", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 23))
      ),
      extra = None)
    ),
    Airline("Aer Lingus", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 24)),
        BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
      ),
      extra = None)
    ),
    Airline("Easy Jet", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 45, 25))
      ),
      extra = None)
    ),
    Airline("Ryan Air", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 20)),
        BaggageAllowance(SmallBag, None, BaggageSize(35, 20, 20))
      ),
      extra = Some("Duty free bags are permitted in the cabin along with your cabin baggage."))
    ),
    Airline("Transavia", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 25))
      ),
      extra = None)
    ),
    Airline("United Airlines", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 35, 22))
      ),
      extra = None)
    ),
    Airline("Wizz Air", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 23))
      ),
      extra = None)
    )
  )

  def baggagePolicy(airlineName: String): F[Airline] = {
    val ifEmpty: F[Airline] = Effect[F].raiseError(AirlineNotFound(airlineName))
    airlines.find(_.name == airlineName).fold(ifEmpty) { policy =>
      Effect[F].delay(policy)
    }
  }

}
