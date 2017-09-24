package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import io.circe.{Encoder, Json}

package object http {

  implicit val visaCategoryEncoder: Encoder[VisaCategory] = Encoder.instance {
    case vr @ VisaRequired                => Json.fromString(vr.toString)
    case vn @ VisaNotRequired             => Json.fromString(vn.toString)
    case ev @ ElectronicVisa              => Json.fromString(ev.toString)
    case oa @ VisaOnArrival               => Json.fromString(oa.toString)
    case ea @ ElectronicVisaPlusOnArrival => Json.fromString(ea.toString)
    case uv @ UnknownVisaCategory         => Json.fromString(uv.toString)
  }

}
