package com.github.gvolpe.smartbackpacker.service

import cats.effect.{Async, Sync}
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.persistence.AirlineDao
import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName, AirlineNotFound}

object AirlineService {
  def apply[F[_] : Async]: AirlineService[F] = new AirlineService[F](AirlineDao[F])
}

class AirlineService[F[_] : Async](airlineDao: AirlineDao[F]) extends AbstractAirlineService[F](airlineDao)

abstract class AbstractAirlineService[F[_] : Async](airlineDao: AirlineDao[F]) {

  def baggagePolicy(airlineName: AirlineName): F[Airline] = {
    val ifEmpty: F[Airline] = Sync[F].raiseError(AirlineNotFound(airlineName.value))
    airlineDao.findAirline(airlineName).>>= { maybeAirline =>
      maybeAirline.fold(ifEmpty) { policy =>
        Sync[F].delay(policy)
      }
    }
  }

}
