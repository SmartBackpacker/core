//package com.github.gvolpe.smartbackpacker.http
//
//import cats.effect._
//import com.github.gvolpe.smartbackpacker.parser.TripAdvisorAirlinesParser
//import io.circe.Json
//import io.circe.generic.auto._
//import io.circe.syntax._
//import org.http4s._
//import org.http4s.circe._
//import org.http4s.dsl._
//
//object AirlineReviewHttpEndpoint {
//
//  val service: HttpService[IO] = HttpService[IO] {
//    case GET -> Root / "airline" / airline =>
//      TripAdvisorAirlinesParser[IO].airlineReviewsFor(airline).attempt.unsafeRunSync() match {
//        case Right(review) => Ok(review.asJson)
//        case Left(error)   => BadRequest(Json.fromString(error.getMessage))
//      }
//  }
//
//}
