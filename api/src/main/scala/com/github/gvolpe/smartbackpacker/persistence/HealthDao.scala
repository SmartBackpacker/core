package com.github.gvolpe.smartbackpacker.persistence

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.functor._
import cats.syntax.option._
import com.github.gvolpe.smartbackpacker.model._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor

class PostgresHealthDao[F[_]](xa: Transactor[F])
                             (implicit F: MonadError[F, Throwable]) extends HealthDao[F] {

  override def findHealthInfo(from: CountryCode): F[Option[Health]] = {
    private val recommendationsStatement: ConnectionIO[VaccineDTO] =
      sql"SELECT v.disease, v.description, v.categories FROM vaccine_recommendations AS vr INNER JOIN vaccine AS v ON vr.vaccine_id=v.id WHERE vr.country_code = ${from.value}"
        .query[VaccineDTO].list

    private val optionalStatement: ConnectionIO[VaccineDTO] =
      sql"SELECT v.disease, v.description, v.categories FROM vaccine_recommendations AS vr INNER JOIN vaccine AS v ON vr.vaccine_id=v.id WHERE vr.country_code = ${from.value}"
        .query[VaccineDTO].list

    private val healthNoticesStatement: ConnectionIO[HealthNoticeDTO] =
      sql"SELECT ha.title, ha.weblink, ha.description FROM health_notice AS hn INNER JOIN health_alert AS ha ON hn.alert_id = ha.id WHERE hn.country_code = ${from.value}"
        .query[HealthNoticeDTO].list

    private val healthAlertStatement: ConnectionIO[HealthAlertDTO] =
      sql"SELECT alert_level FROM health_alert_level WHERE country_code = ${from.value}"
        .query[HealthAlertDTO].unique

    val program: ConnectionIO[Vaccine] =
      for {
        r <- c
        o <- optionalStatement
        n <- healthNoticesStatement
        a <- healthAlertStatement
      } yield {
        val recommendations = r.map(_.toVaccine)
        val optional        = o.map(_.toVaccine)
        val vaccinations    = Vaccinations(recommendations, optional)
        val healthNotices   = n.map(_toHealthNotice(a.as[AlertLevel]))
        Health(vaccinations, healthNotices)
      }

      vaccinations.map(_.toVaccine)

    program.map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[Health].pure[F]
    }
  }

}

class HealthDao[F[_]] {
  def findHealthInfo(countryCode: CountryCode): F[Option[Health]]
}
