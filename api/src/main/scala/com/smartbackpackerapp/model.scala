/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbackpackerapp

object model {

  case class CountryCode(value: String) extends AnyVal {
    override def toString: String = value
  }
  case class CountryName(value: String) extends AnyVal {
    override def toString: String = value
  }
  case class Currency(value: String) extends AnyVal {
    override def toString: String = value
  }
  case class AirlineName(value: String) extends AnyVal {
    override def toString: String = value
  }

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

    def fromName(name: String): VisaCategory = {
      List(
        VisaNotRequired, VisaWaiverProgram, AdmissionRefused, TravelBanned,
        VisaRequired, VisaDeFactoRequired, ElectronicVisa, ElectronicVisitor,
        ElectronicTravelAuthority, FreeVisaOnArrival, VisaOnArrival,
        ElectronicVisaPlusVisaOnArrival, OnlineReciprocityFee,
        MainlandTravelPermit, HomeReturnPermitOnly
      ).find(_.toString == name).getOrElse(UnknownVisaCategory)
    }

  }

  implicit class VisaCategoryOps(value: String) {
    def asVisaCategory: VisaCategory = VisaCategory.parse(value)
  }

  implicit class DescriptionOps(value: String) {
    def asDescription(extra: String): String = {

      def removeBrackets(v: String): String = {
        val regex = """\[[^\]]+\]""".r
        regex.replaceAllIn(v, "")
      }

      if (value.isEmpty && extra.isEmpty) "No more information available"
      else if (value.isEmpty) removeBrackets(extra)
      else if (extra.isEmpty) removeBrackets(value)
      else removeBrackets(s"$value $extra")
    }
  }

  case class Country(code: CountryCode, name: CountryName, currency: Currency)
  case class CountryWithNames(code: CountryCode, names: List[CountryName])

  case class VisaRequirementsData(from: Country,
                                  to: Country,
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

  // Airlines
  sealed trait BaggageType extends Product with Serializable
  case object SmallBag    extends BaggageType
  case object CabinBag    extends BaggageType
  case object CheckedBag  extends BaggageType

  object BaggageType {
    def fromString(value: String): Option[BaggageType] = {
      List(SmallBag, CabinBag, CheckedBag).find(_.toString == value)
    }
  }

  case class BaggageSize(height: Int, width: Int, depth: Int)

  case class BaggageAllowance(baggageType: BaggageType,
                              kgs: Option[Int],
                              size: BaggageSize)

  case class BaggagePolicy(allowance: List[BaggageAllowance],
                           extra: Option[String],
                           website: Option[String])

  case class Airline(name: AirlineName, baggagePolicy: BaggagePolicy)

  // Visa Restriction Index
  sealed trait VisaRestrictionsIndexValues extends Product with Serializable
  case class Rank(value: Int)               extends VisaRestrictionsIndexValues
  case class Countries(names: List[String]) extends VisaRestrictionsIndexValues
  case class PlacesCount(value: Int)        extends VisaRestrictionsIndexValues

  case class Ranking(value: Int) extends AnyVal {
    override def toString: String = value.toString
  }
  case class Count(value: Int) extends AnyVal {
    override def toString: String = value.toString
  }
  case class Sharing(value: Int) extends AnyVal {
    override def toString: String = value.toString
  }

  case class VisaRestrictionsIndex(rank: Ranking, count: Count, sharing: Sharing)

  // Health Information
  case class Disease(value: String) extends AnyVal {
    override def toString: String = value
  }
  case class WebLink(value: String) extends AnyVal {
    override def toString: String = value
  }

  sealed trait AlertLevel extends Product with Serializable
  case object LevelOne extends AlertLevel
  case object LevelTwo extends AlertLevel
  case object NoAlert extends AlertLevel

  object AlertLevel {
    def fromString(value: String): AlertLevel =
      List(LevelOne, LevelTwo).find(_.toString == value).getOrElse(NoAlert)
  }

  sealed trait DiseaseCategory extends Product with Serializable
  case object AvoidNonSterileEquipment extends DiseaseCategory
  case object TakeAntimalarialMeds extends DiseaseCategory
  case object GetVaccinated extends DiseaseCategory
  case object AvoidSharingBodyFluids extends DiseaseCategory
  case object ReduceExposureToGerms extends DiseaseCategory
  case object PreventBugBites extends DiseaseCategory
  case object EatAndDrinkSafely extends DiseaseCategory
  case object KeepAwayFromAnimals extends DiseaseCategory
  case object UnknownDiseaseCategory extends DiseaseCategory

  object DiseaseCategory {
    def fromString(value: String): DiseaseCategory =
      List(
        AvoidNonSterileEquipment, TakeAntimalarialMeds, GetVaccinated,
        AvoidSharingBodyFluids, ReduceExposureToGerms, EatAndDrinkSafely,
        PreventBugBites, KeepAwayFromAnimals
      ).find(_.toString == value).getOrElse(UnknownDiseaseCategory)
  }

  case class Vaccine(disease: Disease, description: String, diseaseCategories: List[DiseaseCategory])
  case class Vaccinations(mandatory: List[Vaccine], recommendations: List[Vaccine], optional: List[Vaccine])
  case class HealthAlert(title: String, link: WebLink, description: String)
  case class HealthNotices(alertLevel: AlertLevel, alerts: List[HealthAlert])
  case class Health(vaccinations: Vaccinations, notices: HealthNotices)

}
