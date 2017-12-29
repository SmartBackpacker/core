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

package com.github.gvolpe.smartbackpacker.scraper

import com.github.gvolpe.smartbackpacker.model.Vaccine
import com.github.gvolpe.smartbackpacker.scraper.model.VisaRequirementsFor

package object sql {

  type VisaRequirementsDTO = (String, String, String, String)

  type VaccineDTO = (String, String, String)

  implicit class VisaRequirementsConversions(vr: VisaRequirementsFor) {
    def toVisaRequirementsDTO: VisaRequirementsDTO =
      (vr.from.value, vr.to.value, vr.visaCategory.toString, vr.description)
  }

  implicit class VaccineConversions(v: Vaccine) {
    def toVaccineDTO: VaccineDTO =
      (v.disease.value, v.description, v.diseaseCategories.map(_.toString).mkString(","))
  }

}
