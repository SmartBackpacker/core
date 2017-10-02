package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import io.circe.{Encoder, Json}

package object http {

  implicit val visaCategoryEncoder: Encoder[VisaCategory] = Encoder.instance {
    case vn @ VisaNotRequired             => Json.fromString(vn.toString)
    case ar @ AdmissionRefused            => Json.fromString(ar.toString)
    case vr @ VisaRequired                => Json.fromString(vr.toString)
    case vf @ VisaDeFactoRequired         => Json.fromString(vf.toString)
    case ev @ ElectronicVisa              => Json.fromString(ev.toString)
    case er @ ElectronicVisitor           => Json.fromString(er.toString)
    case fv @ FreeVisaOnArrival           => Json.fromString(fv.toString)
    case oa @ VisaOnArrival               => Json.fromString(oa.toString)
    case ea @ ElectronicVisaPlusOnArrival => Json.fromString(ea.toString)
    case or @ OnlineReciprocityFee        => Json.fromString(or.toString)
    case uv @ UnknownVisaCategory         => Json.fromString(uv.toString)
  }

}
