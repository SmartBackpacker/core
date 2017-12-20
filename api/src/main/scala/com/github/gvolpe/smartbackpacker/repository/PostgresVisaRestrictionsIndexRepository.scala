package com.github.gvolpe.smartbackpacker.repository

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.option._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRestrictionsIndexRepository
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor

class PostgresVisaRestrictionsIndexRepository[F[_]](xa: Transactor[F])
                                                   (implicit F: MonadError[F, Throwable]) extends VisaRestrictionsIndexRepository[F] {

  override def findRestrictionsIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]] = {
    val indexStatement: ConnectionIO[RestrictionsIndexDTO] =
      sql"SELECT rank, acc, sharing FROM visa_restrictions_index WHERE country_code = ${countryCode.value}"
        .query[RestrictionsIndexDTO].unique

    indexStatement.map(_.toVisaRestrictionsIndex).map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[VisaRestrictionsIndex].pure[F]
    }
  }

}