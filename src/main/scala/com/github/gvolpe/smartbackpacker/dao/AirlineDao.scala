package com.github.gvolpe.smartbackpacker.dao

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.model._

object AirlineDao {
  def apply[F[_] : Effect]: AirlineDao[F] = new InMemoryAirlineDao[F]()
}

class InMemoryAirlineDao[F[_] : Effect] extends AirlineDao[F] {

  /**
    * https://wikitravel.org/en/Discount_airlines_in_Europe
    *
    * https://www.aerlingus.com/travel-information/baggage-information/cabin-baggage/
    * https://www.airbaltic.com/en/hand-baggage
    * https://www.airberlin.com/en/site/landingpages/baggage_services.php
    * https://www.aircanada.com/ca/en/aco/home/plan/baggage/carry-on.html
    * https://www.aireuropa.com/en/flights/baggage
    * https://www.airfrance.fr/FR/en/common/guidevoyageur/pratique/bagages-cabine-airfrance.htm
    * https://www.airserbia.com/en/hand-baggage
    *
    * https://www.blueairweb.com/en/gb/luggage/
    * https://www.britishairways.com/en-gb/information/baggage-essentials/hand-baggage-allowances
    *
    * http://www.easyjet.com/en/help/baggage/cabin-bag-and-hold-luggage
    * https://www.emirates.com/english/before-you-fly/baggage/cabin-baggage-rules.aspx
    * http://www.etihad.com/en-ae/before-you-fly/baggage-information/allowances/
    *
    * https://www.finnair.com/hk/gb/information-services/baggage/carry-on-baggage
    *
    * http://www.iberia.com/gb/luggage/hand-luggage/
    *
    * https://www.klm.com/travel/gb_en/prepare_for_travel/baggage/baggage_allowance/index.htm
    *
    * http://www.lot.com/us/en/carry-on-baggage
    * http://www.lufthansa.com/us/en/Free-baggage-rules
    *
    * https://www.norwegian.com/uk/travel-info/baggage/hand-baggage/
    *
    * https://www.flypgs.com/en/travel-services/flight-services/additional-baggage
    *
    * https://www.ryanair.com/gb/en/plan-trip/flying-with-us/baggage-policy
    *
    * https://www.transavia.com/en-EU/service/hand-luggage/
    * https://p.turkishairlines.com/en-us/any-questions/carry-on-baggage/index.html
    * https://p.turkishairlines.com/en-us/any-questions/free-baggage/index.html
    *
    * https://www.united.com/CMS/en-US/travel/Pages/BaggageCarry-On.aspx
    *
    * https://wizzair.com/en-gb/information-and-services/travel-information/baggage#after-october
    * https://wowair.co.uk/customer-service/faq/luggage/how-much-hand-luggage-can-i-take-me/
    * */

  private val airlines = List(

    Airline("Aer Lingus", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 24)),
        BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
      ),
      extra = None)
    ),

    Airline("Air Baltic", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 20)),
        BaggageAllowance(SmallBag, None, BaggageSize(30, 40, 10))
      ),
      extra = Some("The combined weight of Cabin Bag and Small Bag must not exceed 8 kg."))
    ),

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

    Airline("Air Europa", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 35, 25)),
        BaggageAllowance(SmallBag, None, BaggageSize(20, 35, 30))
      ),
      extra = Some("Applies to Lite Fare class."))
    ),

    Airline("Air France", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 35, 25)),
        BaggageAllowance(SmallBag, None, BaggageSize(40, 30, 15))
      ),
      extra = Some("The combined weight of your hand baggage item and accessory must not exceed 12 kg or 18 kg, depending on your travel cabin. You can take 1 or 2 hand baggage items depending on your flight cabin, as well as 1 accessory."))
    ),

    Airline("Air Serbia", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 20))
      ),
      extra = Some("Small Bag is allowed, no size specified. Small quantity of duty-free goods allowed."))
    ),

    Airline("Blue Air", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 20))
      ),
      extra = Some("You're welcome to bring your duty free products separately from your carry-on bag."))
    ),

    Airline("British Airways", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(23), BaggageSize(56, 45, 25)),
        BaggageAllowance(SmallBag, Some(23), BaggageSize(40, 30, 15))
      ),
      extra = Some("For flights from / to Brazil the Small Bag size is 45 x 36 x 20cm."))
    ),

    Airline("Easy Jet", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 45, 25))
      ),
      extra = None)
    ),

    Airline("Emirates", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(7), BaggageSize(55, 38, 20))
      ),
      extra = Some("Duty free purchases are also permitted in reasonable quantities for all service classes."))
    ),

    Airline("Etihad Airways", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CheckedBag, Some(23), BaggageSize(45, 74, 90)),
        BaggageAllowance(CabinBag, Some(7), BaggageSize(50, 40, 25))
      ),
      extra = Some("A Small Bag of max. 5 kg is also allowed."))
    ),

    Airline("FinnAir", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 45, 25)),
        BaggageAllowance(SmallBag, None, BaggageSize(40, 30, 15))
      ),
      extra = Some("Maximum combined weight 8 kg."))
    ),

    Airline("Iberia", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 45, 25))
      ),
      extra = Some("A Small Bag is also allowed. No size specified."))
    ),

    Airline("KLM", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 35, 25)),
        BaggageAllowance(SmallBag, None, BaggageSize(40, 30, 15))
      ),
      extra = Some("Total weight max. 12 kg."))
    ),

    Airline("LOT Polish Airlines", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 23)),
        BaggageAllowance(SmallBag, None, BaggageSize(40, 35, 12))
      ),
      extra = None)
    ),

    Airline("Lufthansa", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 23))
      ),
      extra = Some("The maximum size per piece of baggage, regardless of class, is 158 cm (width + height + depth)."))
    ),

    Airline("Norwegian", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 23)),
        BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
      ),
      extra = Some("Max. combined weight: 10 kg. Travelling to / from Dubai? Your hand baggage must not exceed 8 kg."))
    ),

    Airline("Pegasus Airlines", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 20))
      ),
      extra = Some("Only for International flights. For Essential class the allowance is 15 kg for domestic flights and 20 kg for international flights."))
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

    Airline("Turkish Airlines", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CheckedBag, Some(20), BaggageSize(55, 53, 50)),
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 23))
      ),
      extra = Some("On flights from Brazil or Argentina to Turkey or beyond, the baggage allowance is 2 pieces. These may weigh up to 32 kg each for both Business Class and Economy Class passengers."))
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
    ),

    Airline("Wow Air", BaggagePolicy(
      allowance = List(
        BaggageAllowance(SmallBag, Some(10), BaggageSize(42, 32, 25))
      ),
      extra = None)
    )

  )

  override def findAirline(airlineName: String): F[Option[Airline]] = Effect[F].delay {
    airlines.find(_.name == airlineName)
  }

}

abstract class AirlineDao[F[_] : Effect] {

  def findAirline(airlineName: String): F[Option[Airline]]

}
