package com.github.gvolpe.smartbackpacker.airlines.parser

import java.nio.file.Paths

import cats.effect.Sync
import cats.instances.string._
import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName, BaggageAllowance, BaggagePolicy, BaggageSize, BaggageType}
import fs2._

object AirlinesFileParser {
  def apply[F[_] : Sync](airlineFile: AirlineFile, allowanceFile: AllowanceFile): AirlinesFileParser[F] =
    new AirlinesFileParser[F](airlineFile, allowanceFile)
}

class AirlinesFileParser[F[_] : Sync](airlineFile: AirlineFile, allowanceFile: AllowanceFile) {

  private val parseAirline: Stream[F, Option[AirlineDTO]] =
    io.file.readAll[F](Paths.get(airlineFile.value), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .map(_.split('|').toList)
      .map(_.toAirlineDTO)

  private val parseAllowance: Stream[F, Option[AirlineAllowanceDTO]] =
    io.file.readAll[F](Paths.get(allowanceFile.value), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .map(_.split('|').toList)
      .map(_.toAirlineAllowanceDTO)

  private val allowance: Stream[F, (String, BaggageAllowance)] = parseAllowance flatMap {
    case Some(dto) =>
      val ba = BaggageAllowance(
        baggageType = BaggageType.fromString(dto.baggageType).orNull,
        kgs = dto.kgs,
        size = BaggageSize(dto.height.getOrElse(0), dto.width.getOrElse(0), dto.depth.getOrElse(0))
      )
      Stream.emit((dto.name, ba))
    case None =>
      Stream.empty
  }

  private val allowanceGroupedByName: Stream[F, List[(String, BaggageAllowance)]] =
    allowance.groupBy(_._1).map(_._2.toList)

  val airlines: Stream[F, Airline] = parseAirline flatMap {
    case Some(dto) =>
      val bas = allowanceGroupedByName.filter(_.map(_._1).contains(dto.name))
      bas map { ba =>
        Airline(
          name = new AirlineName(dto.name),
          baggagePolicy = BaggagePolicy(
            allowance = ba.map(_._2),
            extra = dto.extra,
            website = dto.website
          )
        )
      }
    case None =>
      Stream.empty
  }

}