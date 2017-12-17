package com.github.gvolpe.smartbackpacker.scraper

import com.github.gvolpe.smartbackpacker.scraper.model.VisaRequirementsFor

package object sql {

  type VisaRequirementsDTO = (String, String, String, String)

  implicit class VisaRequirementsConversions(vr: VisaRequirementsFor) {
    def toVisaRequirementsDTO: VisaRequirementsDTO =
      (vr.from.value, vr.to.value, vr.visaCategory.toString, vr.description)
  }

}
