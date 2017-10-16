package com.github.gvolpe.smartbackpacker

import com.github.gvolpe.smartbackpacker.model._
import io.circe.{Encoder, Json}

package object http {

  implicit val visaCategoryEncoder: Encoder[VisaCategory] = Encoder.instance {
    case vn @ VisaNotRequired             => Json.fromString(vn.toString)
    case vw @ VisaWaiverProgram           => Json.fromString(vw.toString)
    case ar @ AdmissionRefused            => Json.fromString(ar.toString)
    case tb @ TravelBanned                => Json.fromString(tb.toString)
    case vr @ VisaRequired                => Json.fromString(vr.toString)
    case vf @ VisaDeFactoRequired         => Json.fromString(vf.toString)
    case ev @ ElectronicVisa              => Json.fromString(ev.toString)
    case er @ ElectronicVisitor           => Json.fromString(er.toString)
    case et @ ElectronicTravelAuthority   => Json.fromString(et.toString)
    case fv @ FreeVisaOnArrival           => Json.fromString(fv.toString)
    case oa @ VisaOnArrival               => Json.fromString(oa.toString)
    case ea @ ElectronicVisaPlusOnArrival => Json.fromString(ea.toString)
    case or @ OnlineReciprocityFee        => Json.fromString(or.toString)
    case mt @ MainlandTravelPermit        => Json.fromString(mt.toString)
    case hr @ HomeReturnPermitOnly        => Json.fromString(hr.toString)
    case uv @ UnknownVisaCategory         => Json.fromString(uv.toString)
  }

  implicit val baggageTypeEncoder: Encoder[BaggageType] = Encoder.instance {
    case bag @ SmallBag    => Json.fromString(bag.toString)
    case bag @ CabinBag    => Json.fromString(bag.toString)
    case bag @ CheckedBag  => Json.fromString(bag.toString)
  }

}
