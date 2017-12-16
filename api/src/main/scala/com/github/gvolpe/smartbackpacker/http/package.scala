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
    visaCategory => Json.fromString(visaCategory.toString)
  }

  implicit val baggageTypeEncoder: Encoder[BaggageType] = Encoder.instance {
    bag => Json.fromString(bag.toString)
  }

}
