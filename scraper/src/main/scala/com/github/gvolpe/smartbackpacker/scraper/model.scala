package com.github.gvolpe.smartbackpacker.scraper

import com.github.gvolpe.smartbackpacker.model.{CountryCode, CountryName, DiseaseCategory, VisaCategory}

object model {

  // Visa Requirements
  case class VisaRequirementsParsing(to: CountryName,
                                     visaCategory: VisaCategory,
                                     description: String)

  case class VisaRequirementsFor(from: CountryCode,
                                 to: CountryCode,
                                 visaCategory: VisaCategory,
                                 description: String)

  // Visa Restrictions Index
  case class VisaRestrictionsRanking(rank: Int, countries: List[String], count: Int)

  // Health Info
  sealed trait HealthInfoRow extends Product with Serializable

  sealed trait HealthInfoGroup extends HealthInfoRow
  case object AllTravelers extends HealthInfoGroup
  case object MostTravelers extends HealthInfoGroup
  case object SomeTravelers extends HealthInfoGroup

  case class DiseaseName(value: String) extends HealthInfoRow
  case class DiseaseDescription(value: String) extends HealthInfoRow
  case class DiseaseCategories(value: List[DiseaseCategory]) extends HealthInfoRow

  case class TravelHealthNotice(title: String, summary: String)
  case class TravelHealthNotices(level: String, notices: List[TravelHealthNotice])

  // Errors
  case class WikiPageNotFound(wikiPage: String) extends Exception(s"Wiki Page not found for $wikiPage")
  case class HealthPageNotFound(healthPage: String) extends Exception(s"Health Page not found for $healthPage")

}
