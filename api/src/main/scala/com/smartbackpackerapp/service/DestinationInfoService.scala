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

package com.smartbackpackerapp.service

import cats.data.EitherT
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.{MonadError, Parallel}
import com.smartbackpackerapp.config.SBConfiguration
import com.smartbackpackerapp.model.{CountryCode, Currency, DestinationInfo, ExchangeRate, VisaRequirements, VisaRequirementsData}
import com.smartbackpackerapp.repository.algebra.VisaRequirementsRepository

class DestinationInfoService[F[_]](sbConfig: SBConfiguration[F],
                                   visaRequirementsRepo: VisaRequirementsRepository[F],
                                   exchangeRateService: AbstractExchangeRateService[F])
                                  (implicit F: MonadError[F, Throwable], P: Parallel[F, F]) {

  def find(from: CountryCode,
           to: CountryCode,
           baseCurrency: Currency): F[ValidationError Either DestinationInfo] = {
    val result =
      for {
        _  <- EitherT.fromEither(validateCountries(from, to))
        fc <- EitherT.liftF(sbConfig.countryCurrency(to, default = Currency("EUR")))
        rs <- EitherT(retrieveDestinationInfoInParallel(from, to, baseCurrency, fc))
      } yield rs

    result.value
  }

  private def retrieveDestinationInfoInParallel(from: CountryCode,
                                                to: CountryCode,
                                                baseCurrency: Currency,
                                                foreignCurrency: Currency): F[ValidationError Either DestinationInfo] = {
    (visaRequirementsFor(from, to), exchangeRateService.exchangeRateFor(baseCurrency, foreignCurrency)).parMapN {
      case (Right(vr), er) =>
        DestinationInfo(
          countryName = vr.to.name,
          countryCode = vr.to.code,
          visaRequirements = VisaRequirements(vr.visaCategory, vr.description),
          exchangeRate = ExchangeRate(Currency(er.base), foreignCurrency, er.rates.getOrElse(foreignCurrency.value, -1.0))
        ).asRight[ValidationError]
      case (Left(e), _) =>
        e.asLeft[DestinationInfo]
    }
  }

  private def validateCountries(from: CountryCode, to: CountryCode): Either[ValidationError, (CountryCode, CountryCode)] = {
    if (from != to) (from, to).asRight
    else CountriesMustBeDifferent.asLeft
  }

  private def visaRequirementsFor(from: CountryCode, to: CountryCode): F[ValidationError Either VisaRequirementsData] =
    visaRequirementsRepo.findVisaRequirements(from, to) map { data =>
      data.toRight[ValidationError](CountryNotFound(to))
    }

}