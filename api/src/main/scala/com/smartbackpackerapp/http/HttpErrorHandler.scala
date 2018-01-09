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

package com.smartbackpackerapp.http

import cats.Monad
import com.smartbackpackerapp.service._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.Response

class HttpErrorHandler[F[_] : Monad] extends Http4sDsl[F] {

  val handle: ValidationError => F[Response[F]] = {
    case CountriesMustBeDifferent           => BadRequest(ApiError(ApiErrorCode.SAME_COUNTRIES_SEARCH, "Countries must be different!").asJson)
    case CountryNotFound(cc)                => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Country not found ${cc.value}").asJson)
    case AirlineNotFound(a)                 => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Airline not found ${a.value}").asJson)
    case VisaRestrictionsIndexNotFound(cc)  => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Visa restrictions index not found for ${cc.value}").asJson)
    case HealthInfoNotFound(cc)             => NotFound(ApiError(ApiErrorCode.ENTITY_NOT_FOUND, s"Health info not found for ${cc.value}").asJson)
  }

}
