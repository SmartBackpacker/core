package com.github.gvolpe.smartbackpacker.scraper

object ScraperJob extends App {

  // Scheduler.oncePerWeek(runJob)

  // TODO
  // - Create Table `visa_requirements`.
  // - Run WikiPageParser once a week for every country and persist the results to PostgreSQL.
  // - Same for VisaRestrictionsIndexParser.
  // - Make sure to have at least the DB backup of a month (4 backups) for resiliency.
  // - Run the parsers in parallel to maximize throughput.
  // - Create CountryDao and VisaRestrictionIndexDao
  // - Modify CountryService and VisaRestrictionIndexService to use the DAOs instead of the Parsers.

}
