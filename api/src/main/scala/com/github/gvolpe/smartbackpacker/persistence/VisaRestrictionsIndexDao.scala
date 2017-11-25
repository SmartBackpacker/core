package com.github.gvolpe.smartbackpacker.persistence

import cats.effect.Async
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.option._
import com.github.gvolpe.smartbackpacker.model._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor

class PostgresVisaRestrictionsIndexDao[F[_] : Async](xa: Transactor[F]) extends VisaRestrictionsIndexDao[F] {

  override def findIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]] = {
    val indexStatement: ConnectionIO[RestrictionsIndexDTO] =
      sql"SELECT rank, acc, sharing FROM visa_restrictions_index WHERE country_code = ${countryCode.value}"
        .query[RestrictionsIndexDTO].unique

    indexStatement.map(_.toVisaRestrictionsIndex).map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[VisaRestrictionsIndex].pure[F]
    }
  }

}

trait VisaRestrictionsIndexDao[F[_]] {
  def findIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]]
}