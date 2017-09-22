package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import io.circe.{Encoder, Json}
import shapeless.Unwrapped

package object http {

  implicit def encodeAnyVal[T, U](implicit ev: T <:< AnyVal,
                                  unwrapped: Unwrapped.Aux[T, U],
                                  encoder: Encoder[U]): Encoder[T] =
    Encoder.instance[T](value => encoder(unwrapped.unwrap(value)))

  implicit val visaCategoryEncoder: Encoder[VisaCategory] = Encoder.instance {
    case vr @ VisaRequired                => Json.fromString(vr.toString)
    case vn @ VisaNotRequired             => Json.fromString(vn.toString)
    case ev @ ElectronicVisa              => Json.fromString(ev.toString)
    case oa @ VisaOnArrival               => Json.fromString(oa.toString)
    case ea @ ElectronicVisaPlusOnArrival => Json.fromString(ea.toString)
    case uv @ UnknownVisaCategory         => Json.fromString(uv.toString)
  }

}
