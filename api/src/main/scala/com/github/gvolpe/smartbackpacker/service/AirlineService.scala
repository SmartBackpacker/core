/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
