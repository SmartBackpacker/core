package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import io.circe.{Encoder, Json}

package object http {

  implicit val countryCodeEncoder: Encoder[CountryCode] = Encoder.instance {
    x => Json.fromString(x.value)
  }

  implicit val countryNameEncoder: Encoder[CountryName] = Encoder.instance {
    x => Json.fromString(x.value)
  }

  implicit val countryLanguageEncoder: Encoder[Language] = Encoder.instance {
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
