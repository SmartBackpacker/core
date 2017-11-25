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

class PostgresAirlineDao[F[_] : Async](xa: Transactor[F]) extends AirlineDao[F] {

  override def findAirline(airlineName: AirlineName): F[Option[Airline]] = {
    val airlineStatement: ConnectionIO[AirlineDTO] =
      sql"SELECT a.airline_id, a.name, b.policy_id, b.extra, b.website FROM airline AS a INNER JOIN baggage_policy AS b ON (a.airline_id = b.airline_id) WHERE a.name=${airlineName.value}"
        .query[AirlineDTO].unique

    val baggageAllowanceStatement: Int => ConnectionIO[List[BaggageAllowanceDTO]] = policyId =>
      sql"SELECT baggage_type, kgs, height, width, depth FROM baggage_allowance WHERE policy_id=$policyId"
        .query[BaggageAllowanceDTO].list

    val program: ConnectionIO[Airline] = for {
      a <- airlineStatement
      b <- baggageAllowanceStatement(a.head)
    } yield a.toAirline(b)

    program.map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => none[Airline].pure[F]
    }
  }

}

trait AirlineDao[F[_]] {
  def findAirline(airlineName: AirlineName): F[Option[Airline]]
}
