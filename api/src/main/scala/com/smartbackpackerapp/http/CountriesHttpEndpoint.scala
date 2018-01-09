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
import cats.syntax.flatMap._
import com.smartbackpackerapp.service.CountryService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class CountriesHttpEndpoint[F[_] : Monad](countryService: CountryService[F]) extends Http4sDsl[F] {

  object BaseQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("query")

  val service: AuthedService[String, F] = AuthedService {
    case GET -> Root / ApiVersion / "countries" :? BaseQueryParamMatcher(query) as _ =>
      val schengen = query.fold(false)(_.equalsIgnoreCase("schengen"))
      countryService.findAll(schengen).flatMap(x => Ok(x.asJson))
  }

}
