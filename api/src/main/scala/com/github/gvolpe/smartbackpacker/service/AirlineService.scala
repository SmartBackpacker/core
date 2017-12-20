package com.github.gvolpe.smartbackpacker.service

import cats.effect.Sync
import cats.syntax.applicative._
import cats.syntax.flatMap._
import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName}
import com.github.gvolpe.smartbackpacker.repository.algebra.AirlineRepository

class AirlineService[F[_] : Sync](airlineRepo: AirlineRepository[F]) {

  def baggagePolicy(airlineName: AirlineName): F[Airline] = {
    val ifEmpty = Sync[F].raiseError[Airline](AirlineNotFound(airlineName))
    airlineRepo.findAirline(airlineName).flatMap { maybeAirline =>
      maybeAirline.fold(ifEmpty)(_.pure[F])
    }
  }

}
