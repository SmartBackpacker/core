package com.github.gvolpe.smartbackpacker.http

import cats.Monad
import com.github.gvolpe.smartbackpacker.service.{ApiServiceError, AirlineNotFound, CountriesMustBeDifferent, CountryNotFound}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.Response

import scala.reflect.ClassTag

// TODO: Change API to have signatures F[Either[ApiServiceError, A]]
class HttpErrorHandler[F[_] : Monad] extends Http4sDsl[F] {

  private val errorHandler: ApiServiceError => F[Response[F]] = {
    case CountriesMustBeDifferent => BadRequest(ApiError(ApiErrorCode.SAME_COUNTRIES_SEARCH, "Countries must be different!").asJson)
    case CountryNotFound(cc)      => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Country not found ${cc.value}").asJson)
    case AirlineNotFound(a)       => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Airline not found ${a.value}").asJson)
  }

  private def liftToPF[X <: Y, W, Y](f: Function[X, W])(implicit ct: ClassTag[X]): PartialFunction[Y, W] =
    new PartialFunction[Y, W] {
      override def isDefinedAt(x: Y): Boolean = ct.runtimeClass.isInstance(x)
      override def apply(v1: Y): W = f(v1.asInstanceOf[X])
    }

  // To use with ApplicativeError.recoverWith
  val handler: PartialFunction[Throwable, F[Response[F]]] = liftToPF(errorHandler)

}
