package com.github.gvolpe.smartbackpacker.scraper.sql

import cats.effect.Async
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.scraper.model._
import com.github.gvolpe.smartbackpacker.scraper.parser.AbstractHealthInfoParser
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

class HealthInfoInsertData[F[_]](xa: Transactor[F],
                                 healthInfoParser: AbstractHealthInfoParser[F])
                                 (implicit F: Async[F]) {

  // Vaccines
  private def insertVaccine(vaccine: Vaccine): ConnectionIO[Int] = {
    val dto = vaccine.toVaccineDTO
    sql"INSERT INTO vaccine (disease, description, categories) VALUES (${dto._1}, ${dto._2}, ${dto._3})"
      .update.withUniqueGeneratedKeys("id")
  }

  private def insertVaccineMandatory(countryId: Int, vaccineId: Int): ConnectionIO[Int] = {
    sql"INSERT INTO vaccine_mandatory (country_id, vaccine_id) VALUES ($countryId, $vaccineId)"
      .update.run
  }

  private def insertVaccineRecommendation(countryId: Int, vaccineId: Int): ConnectionIO[Int] = {
    sql"INSERT INTO vaccine_recommendations (country_id, vaccine_id) VALUES ($countryId, $vaccineId)"
      .update.run
  }

  private def insertVaccineOptional(countryId: Int, vaccineId: Int): ConnectionIO[Int] = {
    sql"INSERT INTO vaccine_optional (country_id, vaccine_id) VALUES ($countryId, $vaccineId)"
      .update.run
  }

  private def insertVaccinationsBulk(countryId: Int, vaccines: List[Vaccine])
                                    (f: (Int, Int) => ConnectionIO[Int]): F[Int] = {
    val result = vaccines.traverse { v =>
      for {
        id <- insertVaccine(v).transact(xa)
        _  <- f(countryId, id).transact(xa)
      } yield ()
    }
    result.map(_ => vaccines.size)
  }

  private def insertVaccineMandatoryBulk(countryId: Int, vaccines: List[Vaccine]): F[Int] = {
    insertVaccinationsBulk(countryId, vaccines)(insertVaccineMandatory)
  }

  private def insertVaccineRecommendationsBulk(countryId: Int, vaccines: List[Vaccine]): F[Int] = {
    insertVaccinationsBulk(countryId, vaccines)(insertVaccineRecommendation)
  }

  private def insertVaccineOptionalBulk(countryId: Int, vaccines: List[Vaccine]): F[Int] = {
    insertVaccinationsBulk(countryId, vaccines)(insertVaccineOptional)
  }

  private def findCountryId(countryCode: CountryCode): ConnectionIO[Int] = {
    sql"SELECT id FROM countries WHERE code = ${countryCode.value}"
      .query[Int].unique
  }

  // Health Notices
  private def insertAlertLevel(countryId: Int, alertLevel: AlertLevel): ConnectionIO[Int] = {
    sql"INSERT INTO health_alert_level (country_id, alert_level) VALUES ($countryId, ${alertLevel.toString})"
      .update.run
  }

  private def insertHealthAlert(a: HealthAlert): ConnectionIO[Int] = {
    sql"INSERT INTO health_alert (title, weblink, description) VALUES (${a.title}, ${a.link.value}, ${a.description})"
      .update.withUniqueGeneratedKeys("id")
  }

  private def insertHealthNotice(countryId: Int, alertId: Int): ConnectionIO[Int] = {
    sql"INSERT INTO health_notice (country_id, alert_id) VALUES ($countryId, $alertId)"
      .update.run
  }

  private def insertHealthAlertsBulk(countryId: Int, alerts: List[HealthAlert]): F[Int] = {
    val result = alerts.traverse { a =>
      for {
        id <- insertHealthAlert(a).transact(xa)
        _  <- insertHealthNotice(countryId, id).transact(xa)
      } yield ()
    }
    result.map(_ => alerts.size)
  }

  // Insert data Program
  private val errorHandler: PartialFunction[Throwable, F[Unit]] = {
    case HealthPageNotFound(code) =>
      F.delay(println(s"Health page not found for $code"))
  }

  def run(cc: CountryCode): F[Unit] = {
    val program = for {
      _      <- F.delay(println(s"${cc.value} >> Gathering health information from CDC"))
      health <- healthInfoParser.parse(cc)
      _      <- F.delay(println(s"${cc.value} >> Starting data insertion into DB"))
      cid    <- findCountryId(cc).transact(xa)
      rs0    <- insertVaccineMandatoryBulk(cid, health.vaccinations.mandatory)
      rs1    <- insertVaccineRecommendationsBulk(cid, health.vaccinations.recommendations)
      rs2    <- insertVaccineOptionalBulk(cid, health.vaccinations.optional)
      _      <- F.delay(println(s"${cc.value} >> Created $rs0 records for mandatory, $rs1 for recommendations and $rs2 for optional"))
      _      <- insertAlertLevel(cid, health.notices.alertLevel).transact(xa)
      rs3    <- insertHealthAlertsBulk(cid, health.notices.alerts)
      _      <- F.delay(println(s"${cc.value} >> Created $rs3 records for health alerts"))
    } yield ()
    program.recoverWith(errorHandler)
  }

}
