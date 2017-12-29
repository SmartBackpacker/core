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

package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import io.circe.{Encoder, Json}

package object http {

  val ApiVersion = "v1"

  class ApiErrorCode(val value: Int) extends AnyVal

  object ApiErrorCode {
    val ENTITY_NOT_FOUND = new ApiErrorCode(100)
    val SAME_COUNTRIES_SEARCH = new ApiErrorCode(101)
  }

  case class ApiError(code: ApiErrorCode, error: String)

  // --- Json encoders ---
  implicit val apiErrorCodeEncoder: Encoder[ApiErrorCode] = Encoder.instance {
    x => Json.fromString(x.value.toString)
  }

  implicit val countryCodeEncoder: Encoder[CountryCode] = Encoder.instance {
    x => Json.fromString(x.value)
  }

  implicit val countryNameEncoder: Encoder[CountryName] = Encoder.instance {
    x => Json.fromString(x.value)
  }

  implicit val currencyEncoder: Encoder[Currency] = Encoder.instance {
    x => Json.fromString(x.value)
  }

  implicit val airlineNameEncoder: Encoder[AirlineName] = Encoder.instance {
    x => Json.fromString(x.value)
  }

  implicit val visaCategoryEncoder: Encoder[VisaCategory] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

  implicit val baggageTypeEncoder: Encoder[BaggageType] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

  implicit val visaRankingEncoder: Encoder[Ranking] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

  implicit val rankingCountTypeEncoder: Encoder[Count] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

  implicit val rankingSharingEncoder: Encoder[Sharing] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

  implicit val alertLevelEncoder: Encoder[AlertLevel] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

  implicit val diseaseEncoder: Encoder[Disease] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

  implicit val diseaseCategoryEncoder: Encoder[DiseaseCategory] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

  implicit val webLinkEncoder: Encoder[WebLink] = Encoder.instance {
    x => Json.fromString(x.toString)
  }

}
