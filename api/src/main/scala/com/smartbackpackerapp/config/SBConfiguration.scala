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

package com.smartbackpackerapp.config

import cats.effect.Sync
import com.smartbackpackerapp.model.{CountryCode, Currency}
import com.typesafe.config.ConfigFactory

class SBConfiguration[F[_]](implicit F: Sync[F]) {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def fixerBaseUri: F[Option[String]] = F.delay {
    sys.env.get("FIXER_URL").orElse(safeConfig.string("fixer.uri"))
  }

  def countryCurrency(countryCode: CountryCode, default: Currency): F[Currency] = F.delay {
    safeConfig.string(s"countries.currency.${countryCode.value}").map(Currency.apply).getOrElse(default)
  }

}
