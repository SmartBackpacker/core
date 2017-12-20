package com.github.gvolpe.smartbackpacker.service

import cats.effect.Sync
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName}
import com.github.gvolpe.smartbackpacker.repository.algebra.AirlineRepository

class AirlineService[F[_] : Sync](airlineRepo: AirlineRepository[F]) {

  def baggagePolicy(airlineName: AirlineName): F[ValidationError Either Airline] =
    airlineRepo.findAirline(airlineName) map { airline =>
      airline.toRight[ValidationError](AirlineNotFound(airlineName))
    }

}
