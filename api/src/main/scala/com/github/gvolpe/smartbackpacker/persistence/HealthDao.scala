package com.github.gvolpe.smartbackpacker.persistence

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.option._
import com.github.gvolpe.smartbackpacker.model._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor

class PostgresHealthDao[F[_]](xa: Transactor[F])
                             (implicit F: MonadError[F, Throwable]) extends HealthDao[F] {

  override def findHealthInfo(from: CountryCode): F[Option[Health]] = {
    val findCountryId: ConnectionIO[Int] = {
      sql"SELECT id FROM countries WHERE code = ${from.value}"
        .query[Int].unique
    }

    def mandatoryStatement(countryId: Int): ConnectionIO[List[VaccineDTO]] =
      sql"SELECT v.disease, v.description, v.categories FROM vaccine_mandatory AS vm INNER JOIN vaccine AS v ON vm.vaccine_id=v.id WHERE vm.country_id = $countryId"
        .query[VaccineDTO].list

    def recommendationsStatement(countryId: Int): ConnectionIO[List[VaccineDTO]] =
      sql"SELECT v.disease, v.description, v.categories FROM vaccine_recommendations AS vr INNER JOIN vaccine AS v ON vr.vaccine_id=v.id WHERE vr.country_id = $countryId"
        .query[VaccineDTO].list

    def  optionalStatement(countryId: Int): ConnectionIO[List[VaccineDTO]] =
      sql"SELECT v.disease, v.description, v.categories FROM vaccine_optional AS vo INNER JOIN vaccine AS v ON vo.vaccine_id=v.id WHERE vo.country_id = $countryId"
        .query[VaccineDTO].list

    def healthNoticesStatement(countryId: Int): ConnectionIO[List[HealthNoticeDTO]] =
      sql"SELECT ha.title, ha.weblink, ha.description FROM health_notice AS hn INNER JOIN health_alert AS ha ON hn.alert_id = ha.id WHERE hn.country_id = $countryId"
        .query[HealthNoticeDTO].list

    def healthAlertStatement(countryId: Int): ConnectionIO[HealthAlertDTO] =
      sql"SELECT alert_level FROM health_alert_level WHERE country_id = $countryId"
        .query[HealthAlertDTO].unique

    val program: ConnectionIO[Health] =
      for {
        c <- findCountryId
        m <- mandatoryStatement(c)
        r <- recommendationsStatement(c)
        o <- optionalStatement(c)
        n <- healthNoticesStatement(c)
        a <- healthAlertStatement(c)
      } yield {
        val mandatory       = m.map(_.toVaccine)
        val recommendations = r.map(_.toVaccine)
        val optional        = o.map(_.toVaccine)
        val vaccinations    = Vaccinations(mandatory, recommendations, optional)
        val alertLevel      = a.toAlertLevel
        val healthNotices   = n.map(_.toHealthAlert(alertLevel))
        Health(vaccinations, HealthNotices(alertLevel, healthNotices))
      }

    program.map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[Health].pure[F]
    }
  }

}

trait HealthDao[F[_]] {
  def findHealthInfo(countryCode: CountryCode): F[Option[Health]]
}
