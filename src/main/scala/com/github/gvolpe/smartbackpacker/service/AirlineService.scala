package com.github.gvolpe.smartbackpacker.service

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.dao.AirlineDao
import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineNotFound}

object AirlineService {
  def apply[F[_] : Effect]: AirlineService[F] = new AirlineService[F](AirlineDao[F])
}

class AirlineService[F[_] : Effect](airlineDao: AirlineDao[F]) extends AbstractAirlineService[F](airlineDao)

abstract class AbstractAirlineService[F[_] : Effect](airlineDao: AirlineDao[F]) {

  def baggagePolicy(airlineName: String): F[Airline] = {
    val ifEmpty: F[Airline] = Effect[F].raiseError(AirlineNotFound(airlineName))
    Effect[F].flatMap(airlineDao.findAirline(airlineName)) { maybeAirline =>
      maybeAirline.fold(ifEmpty) { policy =>
        Effect[F].delay(policy)
      }
    }
  }

}
