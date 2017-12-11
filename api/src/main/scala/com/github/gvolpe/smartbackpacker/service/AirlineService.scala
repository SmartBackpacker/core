package com.github.gvolpe.smartbackpacker.service

import cats.effect.Sync
import cats.syntax.applicative._
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName, AirlineNotFound}
import com.github.gvolpe.smartbackpacker.persistence.AirlineDao

class AirlineService[F[_] : Sync](airlineDao: AirlineDao[F]) {

  def baggagePolicy(airlineName: AirlineName): F[Airline] = {
    val ifEmpty = Sync[F].raiseError[Airline](AirlineNotFound(airlineName.value))
    airlineDao.findAirline(airlineName).flatMap { maybeAirline =>
      maybeAirline.fold(ifEmpty)(_.pure[F])
    }
  }

}
