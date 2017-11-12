package com.github.gvolpe.smartbackpacker.persistence

import cats.effect.{Async, Sync}
import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName, BaggageAllowance, BaggagePolicy, BaggageSize, CabinBag, SmallBag}
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor

object AirlineDao {
  def apply[F[_] : Async]: AirlineDao[F] = new PostgresAirlineDao[F]
}

class InMemoryAirlineDao[F[_] : Async] extends AirlineDao[F] {

  private val airlines: List[Airline] = List(
    Airline("Aer Lingus", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, Some(10), BaggageSize(55, 40, 24)),
        BaggageAllowance(SmallBag, None, BaggageSize(25, 33, 20))
      ),
      extra = None,
      website = Some("https://www.aerlingus.com/travel-information/baggage-information/cabin-baggage/"))
    ),
    Airline("Transavia", BaggagePolicy(
      allowance = List(
        BaggageAllowance(CabinBag, None, BaggageSize(55, 40, 25))
      ),
      extra = None,
      website = Some("https://www.transavia.com/en-EU/service/hand-luggage/"))
    )
  )

  override def findAirline(airlineName: AirlineName): F[Option[Airline]] =
    Sync[F].delay {
      airlines.find(_.name == airlineName.value)
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
