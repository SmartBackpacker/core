package com.github.gvolpe.smartbackpacker.persistence.static

import com.github.gvolpe.smartbackpacker.model.{Airline, BaggageAllowance, BaggagePolicy, BaggageSize, CabinBag, CheckedBag, SmallBag}

// See: https://wikitravel.org/en/Discount_airlines_in_Europe
object AirlinesData {

  val airlines: List[Airline] = List(

    Airline("Aer Lingus", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 24)),
        BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
      ),
      extra = None,
      website = Some("https://www.aerlingus.com/travel-information/baggage-information/cabin-baggage/"))
    ),

    Airline("Air Baltic", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 20)),
        BaggageAllowance(SmallBag, None, BaggageSize(30, 40, 10))
      ),
      extra = Some("The combined weight of Cabin Bag and Small Bag must not exceed 8 kg."),
      website = Some("https://www.airbaltic.com/en/hand-baggage"))
    ),

    Airline("Air Berlin", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 23)),
        BaggageAllowance(SmallBag, Some(2), BaggageSize(40, 30, 10))
      ),
      extra = Some("The number of free checked baggage items varies according to the fare booked."),
      website = Some("https://www.airberlin.com/en/site/landingpages/baggage_services.php"))
    ),

    Airline("Air Canada", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 23))
      ),
      extra = None,
      website = Some("https://www.aircanada.com/ca/en/aco/home/plan/baggage/carry-on.html"))
    ),

    Airline("Air Europa", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 35, 25)),
        BaggageAllowance(SmallBag, None, BaggageSize(20, 35, 30))
      ),
      extra = Some("Applies to Lite Fare class."),
      website = Some("https://www.aireuropa.com/en/flights/baggage"))
    ),

    Airline("Air France", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 35, 25)),
        BaggageAllowance(SmallBag, None, BaggageSize(40, 30, 15))
      ),
      extra = Some("The combined weight of your hand baggage item and accessory must not exceed 12 kg or 18 kg, depending on your travel cabin. You can take 1 or 2 hand baggage items depending on your flight cabin, as well as 1 accessory."),
      website = Some("https://www.airfrance.fr/FR/en/common/guidevoyageur/pratique/bagages-cabine-airfrance.htm"))
    ),

    Airline("Air Serbia", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 20))
      ),
      extra = Some("Small Bag is allowed, no size specified. Small quantity of duty-free goods allowed."),
      website = Some("https://www.airserbia.com/en/hand-baggage"))
    ),

    Airline("Blue Air", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 20))
      ),
      extra = Some("You're welcome to bring your duty free products separately from your carry-on bag."),
      website = Some("https://www.blueairweb.com/en/gb/luggage/"))
    ),

    Airline("British Airways", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(23), BaggageSize(56, 45, 25)),
        BaggageAllowance(SmallBag, Some(23), BaggageSize(40, 30, 15))
      ),
      extra = Some("For flights from / to Brazil the Small Bag size is 45 x 36 x 20cm."),
      website = Some("https://www.britishairways.com/en-gb/information/baggage-essentials/hand-baggage-allowances"))
    ),

    Airline("Easy Jet", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 45, 25))
      ),
      extra = None,
      website = Some("http://www.easyjet.com/en/help/baggage/cabin-bag-and-hold-luggage"))
    ),

    Airline("Emirates", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(7), BaggageSize(55, 38, 20))
      ),
      extra = Some("Duty free purchases are also permitted in reasonable quantities for all service classes."),
      website = Some("https://www.emirates.com/english/before-you-fly/baggage/cabin-baggage-rules.aspx"))
    ),

    Airline("Etihad Airways", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CheckedBag, Some(23), BaggageSize(45, 74, 90)),
        BaggageAllowance(CabinBag, Some(7), BaggageSize(50, 40, 25))
      ),
      extra = Some("A Small Bag of max. 5 kg is also allowed."),
      website = Some("http://www.etihad.com/en-ae/before-you-fly/baggage-information/allowances/"))
    ),

    Airline("FinnAir", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 45, 25)),
        BaggageAllowance(SmallBag, None, BaggageSize(40, 30, 15))
      ),
      extra = Some("Maximum combined weight 8 kg."),
      website = Some("https://www.finnair.com/hk/gb/information-services/baggage/carry-on-baggage"))
    ),

    Airline("Iberia", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 45, 25))
      ),
      extra = Some("A Small Bag is also allowed. No size specified."),
      website = Some("http://www.iberia.com/gb/luggage/hand-luggage/"))
    ),

    Airline("KLM", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 35, 25)),
        BaggageAllowance(SmallBag, None, BaggageSize(40, 30, 15))
      ),
      extra = Some("Total weight max. 12 kg."),
      website = Some("https://www.klm.com/travel/gb_en/prepare_for_travel/baggage/baggage_allowance/index.htm"))
    ),

    Airline("LOT Polish Airlines", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 23)),
        BaggageAllowance(SmallBag, None, BaggageSize(40, 35, 12))
      ),
      extra = None,
      website = Some("http://www.lot.com/us/en/carry-on-baggage"))
    ),

    Airline("Lufthansa", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 23))
      ),
      extra = Some("The maximum size per piece of baggage, regardless of class, is 158 cm (width + height + depth)."),
      website = Some("http://www.lufthansa.com/us/en/Free-baggage-rules"))
    ),

    Airline("Norwegian", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 23)),
        BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
      ),
      extra = Some("Max. combined weight: 10 kg. Travelling to / from Dubai? Your hand baggage must not exceed 8 kg."),
      website = Some("https://www.norwegian.com/uk/travel-info/baggage/hand-baggage/"))
    ),

    Airline("Pegasus Airlines", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 20))
      ),
      extra = Some("Only for International flights. For Essential class the allowance is 15 kg for domestic flights and 20 kg for international flights."),
      website = Some("https://www.flypgs.com/en/travel-services/flight-services/additional-baggage"))
    ),

    Airline("Ryan Air", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 20)),
        BaggageAllowance(SmallBag, None, BaggageSize(35, 20, 20))
      ),
      extra = Some("Duty free bags are permitted in the cabin along with your cabin baggage."),
      website = Some("https://www.ryanair.com/gb/en/plan-trip/flying-with-us/baggage-policy"))
    ),

    Airline("Transavia", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 25))
      ),
      extra = None,
      website = Some("https://www.transavia.com/en-EU/service/hand-luggage/"))
    ),

    Airline("Turkish Airlines", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CheckedBag, Some(20), BaggageSize(55, 53, 50)),
        BaggageAllowance(CabinBag, Some(8), BaggageSize(55, 40, 23))
      ),
      extra = Some("On flights from Brazil or Argentina to Turkey or beyond, the baggage allowance is 2 pieces. These may weigh up to 32 kg each for both Business Class and Economy Class passengers."),
      website = Some("https://p.turkishairlines.com/en-us/any-questions/carry-on-baggage/index.html"))
    ),

    Airline("United Airlines", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(56, 35, 22))
      ),
      extra = None,
      website = Some("https://www.united.com/CMS/en-US/travel/Pages/BaggageCarry-On.aspx"))
    ),

    Airline("Wizz Air", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 23))
      ),
      extra = None,
      website = Some("https://wizzair.com/en-gb/information-and-services/travel-information/baggage#after-october"))
    ),

    Airline("Wow Air", BaggagePolicy(
      allowance = List(
        BaggageAllowance(SmallBag, Some(10), BaggageSize(42, 32, 25))
      ),
      extra = None,
      website = Some("https://wowair.co.uk/customer-service/faq/luggage/how-much-hand-luggage-can-i-take-me/"))
    )

  )

}
