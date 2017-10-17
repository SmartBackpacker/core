package com.github.gvolpe.smartbackpacker

object model {

  type CountryCode  = String
  type CountryName  = String
  type Language     = String
  type Currency     = String

  sealed trait VisaCategory extends Product with Serializable
  case object VisaNotRequired                 extends VisaCategory
  case object VisaWaiverProgram               extends VisaCategory
  case object AdmissionRefused                extends VisaCategory
  case object TravelBanned                    extends VisaCategory
  case object VisaRequired                    extends VisaCategory
  case object VisaDeFactoRequired             extends VisaCategory
  case object ElectronicVisa                  extends VisaCategory
  case object ElectronicVisitor               extends VisaCategory
  case object ElectronicTravelAuthority       extends VisaCategory
  case object FreeVisaOnArrival               extends VisaCategory
  case object VisaOnArrival                   extends VisaCategory
  case object ElectronicVisaPlusVisaOnArrival extends VisaCategory
  case object OnlineReciprocityFee            extends VisaCategory
  case object MainlandTravelPermit            extends VisaCategory
  case object HomeReturnPermitOnly            extends VisaCategory
  case object UnknownVisaCategory             extends VisaCategory

  object VisaCategory {
    def parse(value: String): VisaCategory = value.toLowerCase match {
      case v: String =>
        if (v.contains("admission refused")) AdmissionRefused
        else if (v.contains("travel banned") || v.contains("travel restricted")) TravelBanned
        else if (v.contains("visa waiver program")) VisaWaiverProgram
        else if (v.contains("visa not required") || v.contains("freedom of movement")) VisaNotRequired
        else if (v.contains("visa required") || v.contains("tourist card required") || v.contains("certificate required") || v.contains("identity required") || v.contains("approval required")) VisaRequired
        else if (v.contains("visa de facto required")) VisaDeFactoRequired
        else if (v.contains("evisitor")) ElectronicVisitor
        else if (v.contains("online reciprocity fee")) OnlineReciprocityFee
        else if (v.contains("mainland travel permit")) MainlandTravelPermit
        else if (v.contains("home return permit only")) HomeReturnPermitOnly
        else if ((v.contains("e-visa") || v.contains("evisa") || v.contains("electronic")) && v.contains("on arrival")) ElectronicVisaPlusVisaOnArrival
        else if (v.contains("electronic travel authority")) ElectronicTravelAuthority
        else if (v.contains("e-visa") || v.contains("evisa") || v.contains("electronic")) ElectronicVisa
        else if (v.contains("free") && v.contains("on arrival")) FreeVisaOnArrival
        else if (v.contains("on arrival")) VisaOnArrival
        else UnknownVisaCategory
      case _ => UnknownVisaCategory
    }
  }

  implicit class VisaCategoryOps(value: String) {
    def asVisaCategory: VisaCategory = VisaCategory.parse(value)
  }

  implicit class DescriptionOps(value: String) {
    def asDescription: String = {
      if (value.isEmpty) "No more information available"
      else value
    }
  }

  implicit class CountryOps(value: String) {
    def asCountry: String = {
      value.dropWhile(_.toInt == 160) // Remove whitespaces at the start
    }
  }

  case class VisaRequirementsFor(country: CountryName,
                                 visaCategory: VisaCategory,
                                 description: String)

  case class VisaRequirements(visaCategory: VisaCategory,
                              description: String)

  case class ExchangeRate(baseCurrency: Currency,
                          foreignCurrency: Currency,
                          rate: Double)

  case class DestinationInfo(countryName: CountryName,
                             countryCode: CountryCode,
                             visaRequirements: VisaRequirements,
                             exchangeRate: ExchangeRate)

  case class AirlineReview(name: String,
                           rating: Double,
                           address: String,
                           website: String,
                           logo: String)

  case class CountryNotFound(countryCode: CountryCode) extends Exception(s"Country code not found $countryCode")

  // Airlines
  sealed trait BaggageType extends Product with Serializable
  case object SmallBag extends BaggageType
  case object CabinBag extends BaggageType
  case object CheckedBag extends BaggageType

  case class BaggageSize(height: Int, width: Int, depth: Int)

  case class BaggageAllowance(baggageType: BaggageType,
                              kgs: Option[Int],
                              size: BaggageSize)

  case class BaggagePolicy(allowance: List[BaggageAllowance],
                           extra: Option[String])

  case class Airline(name: String, baggagePolicy: BaggagePolicy)

  case class AirlineNotFound(airlineName: String) extends Exception(s"Airline not found $airlineName")
}
