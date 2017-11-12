package com.github.gvolpe.smartbackpacker.persistence

import cats.effect.{Async, Sync}
import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName}
import com.github.gvolpe.smartbackpacker.persistence.static.AirlinesData
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor

object AirlineDao {
  def apply[F[_] : Async]: AirlineDao[F] = new PostgresAirlineDao[F]
}

class InMemoryAirlineDao[F[_] : Async] extends AirlineDao[F] {

  override def findAirline(airlineName: AirlineName): F[Option[Airline]] =
    Sync[F].delay {
      AirlinesData.airlines.find(_.name == airlineName.value)
    }

}

class PostgresAirlineDao[F[_] : Async] extends AirlineDao[F] {

  import cats.syntax.applicativeError._
  import doobie.free.connection.ConnectionIO
  import doobie.implicits._

  private val xa: Transactor.Aux[F, Unit] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver", "jdbc:postgresql:sb", "postgres", "postgres"
  )

  override def findAirline(airlineName: AirlineName): F[Option[Airline]] = {
    val airlineStatement: ConnectionIO[AirlineDTO] =
      sql"SELECT a.airline_id, a.name, b.policy_id, b.extra, b.website FROM airline AS a INNER JOIN baggage_policy AS b ON (a.airline_id = b.airline_id) WHERE a.name=${airlineName.value}"
        .query[AirlineDTO].unique

    val baggageAllowanceStatement: Int => ConnectionIO[List[AllowanceDTO]] = policyId =>
      sql"SELECT baggage_type, kgs, height, width, depth FROM baggage_allowance WHERE policy_id=$policyId"
        .query[AllowanceDTO].list

    val program: ConnectionIO[Airline] = for {
      a <- airlineStatement
      b <- baggageAllowanceStatement(a.head)
    } yield a.toAirline(b)

    program.map(Option.apply).transact(xa).recoverWith {
      case UnexpectedEnd => Async[F].delay(None)
    }
  }

}

abstract class AirlineDao[F[_] : Async] {

  def findAirline(airlineName: AirlineName): F[Option[Airline]]

}
