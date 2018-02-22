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

package com.smartbackpackerapp.scraper.parser

import cats.effect.IO
import com.smartbackpackerapp.common.IOAssertion
import com.smartbackpackerapp.model._
import com.smartbackpackerapp.scraper.config.ScraperConfiguration
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

import scala.io.Source

class VisaRequirementsParserSpec extends FlatSpecLike with Matchers with VisaRequirementsParserFixture {

  private val scraperConfig = new ScraperConfiguration[IO]

  private val parser = new AbstractVisaRequirementsParser[IO](scraperConfig) {
    override def htmlDocument(from: CountryCode): IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"wikiPageTest-${from.value}.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }
  }

  it should "NOT find the wiki page for a non-existent country code" in IOAssertion {
    parser.visaRequirementsFor(CountryCode("XX")).attempt.map { result =>
      assert(result.isLeft)
    }
  }

  forAll(examples) { (description, from, to, expectedCategory, expectedDescription) =>
    it should description in IOAssertion {
      parser.visaRequirementsFor(CountryCode(from)).map { list =>
        list.filter(_.to.value == to).foreach { req =>
          req.visaCategory should be (expectedCategory)
          req.description  should be (expectedDescription)
        }
      }
    }
  }

  forAll(countries) { to =>
    ignore should s"parse the visa requirements for AR -> $to" in IOAssertion.when(to != "Argentina" && to != "France") { // it's France and territories for AR
      parser.visaRequirementsFor(CountryCode("AR")).map { list =>
        list.foreach { req =>
          req.visaCategory should not be UnknownVisaCategory
          req.description  should not be empty
        }
      }
    }
  }

  forAll(countries) { to =>
    ignore should s"parse the visa requirements for GB -> $to" in IOAssertion.when(to != "United Kingdom") {
      parser.visaRequirementsFor(CountryCode("GB")).map { list =>
        list.foreach { req =>
          req.visaCategory should not be UnknownVisaCategory
          req.description  should not be empty
        }
      }
    }
  }

  forAll(countries) { to =>
    ignore should s"parse the visa requirements for IE -> $to" in IOAssertion.when(to != "Ireland") {
      parser.visaRequirementsFor(CountryCode("IE")).map { list =>
        list.foreach { req =>
          req.visaCategory should not be UnknownVisaCategory
          req.description  should not be empty
        }
      }
    }
  }

  forAll(countries) { to =>
    ignore should s"parse the visa requirements for DE -> $to" in IOAssertion.when(to != "Germany") {
      parser.visaRequirementsFor(CountryCode("DE")).map { list =>
        list.foreach { req =>
          req.visaCategory should not be UnknownVisaCategory
          req.description  should not be empty
        }
      }
    }
  }

  forAll(countries) { to =>
    ignore should s"parse the visa requirements for CA -> $to" in IOAssertion.when(
      to != "Canada" && to != "France" && to != "Australia" // it's France and territories, etc
      && to != "Denmark" && to != "Netherlands" && to != "United Kingdom") {
        parser.visaRequirementsFor(CountryCode("CA")).map { list =>
          list.foreach { req =>
            req.visaCategory should not be UnknownVisaCategory
            req.description  should not be empty
          }
        }
    }
  }

  forAll(countries) { to =>
    ignore should s"parse the visa requirements for ZA -> $to" in IOAssertion.when(to != "South Africa") {
      parser.visaRequirementsFor(CountryCode("ZA")).map { list =>
        list.foreach { req =>
          req.visaCategory should not be UnknownVisaCategory
          req.description  should not be empty
        }
      }
    }
  }

}

trait VisaRequirementsParserFixture extends PropertyChecks {

  val examples = Table(
    ("description", "from", "to", "expectedCategory", "expectedDescription"),
    ("find VisaRequired for rowspan case in Visa Requirements", "CA", "Somalia", VisaRequired, "Visa may be obtained on arrival, provided an invitation letter by the sponsor had been submitted to the authorities before arrival. Due to safety concerns, Canadian government advises its citizens against all travel to Somalia."),
    ("find VisaNotRequired in Visa Requirements", "AR", "Romania", VisaNotRequired, "90 days within any 180 day period"),
    ("find VisaWaiverProgram in Visa Requirements", "IE", "United States", VisaWaiverProgram, "90 days ESTA required if arriving by air or cruise ship. ESTA is valid for two years when issued. Holders of non-biometric passports must obtain a visa for the United States as they are ineligible to apply for an ESTA. Leaving the United States and re-entering from Canada or Mexico will not reset the original 90 day maximum stay. You can only reset the timer by leaving North America."),
    ("find VisaRequired in Visa Requirements", "AR", "Cameroon", VisaRequired, "No more information available"),
    ("find VisaOnArrival in Visa Requirements", "AR", "Egypt", VisaOnArrival, "30 days"),
    ("find ElectronicVisa in Visa Requirements", "AR", "India", ElectronicVisa, "60 days; e-Visa holders must arrive via 24 designated airports or 3 designated seaports."),
    ("find ElectronicVisaPlusOnArrival in Visa Requirements", "AR", "Ethiopia", ElectronicVisaPlusVisaOnArrival, "No more information available"),
    ("find ElectronicVisitor in Visa Requirements", "DE", "Australia", ElectronicVisitor, "90 days on each visit in 12-month period if granted"),
    ("find ElectronicTravelAuthority in Visa Requirements", "SG", "Australia", ElectronicTravelAuthority, "90 days on each visit in 12-month period if granted. May enter using SmartGate on arrival in Australia "),
    ("find FreeVisaOnArrival in Visa Requirements", "DE", "Papua New Guinea", FreeVisaOnArrival, "60 days. Free of charge."),
    ("find VisaDeFactoRequired in Visa Requirements", "FJ", "Andorra", VisaDeFactoRequired, "There are no visa requirements for entry into Andorra, but it can only be accessed by passing through France or Spain. A multiple entry visa is required to re-enter either France or Spain when leaving Andorra. All visitors can stay for 3 months."),
    ("find AdmissionRefused in Visa Requirements", "GN", "Jordan", AdmissionRefused, "No more information available"),
    ("find TravelBanned in Visa Requirements", "KR", "Iraq", TravelBanned, "As of 2007, due to safety concerns, South Korean government bans its citizens from visiting Iraq."),
    ("NOT find Visa Requirements (UnknownVisaCategory)", "AR", "Planet Mars", UnknownVisaCategory, "No information available")
  )

  // TODO: Not working fot Aruba, Bermuda, Gibraltar, Hong Kong, Macao, Taiwan

  val countries = Table(
    "Afghanistan",
    "Albania",
    "Algeria",
    "Andorra",
    "Angola",
    "Antigua and Barbuda",
    "Argentina",
    "Armenia",
    "Australia",
    "Austria",
    "Azerbaijan",
    "Bahamas",
    "Bahrain",
    "Bangladesh",
    "Barbados",
    "Belarus",
    "Belgium",
    "Belize",
    "Benin",
    "Bhutan",
    "Bolivia",
    "Bosnia and Herzegovina",
    "Botswana",
    "Brazil",
    "Brunei",
    "Bulgaria",
    "Burkina Faso",
    "Burundi",
    "Cambodia",
    "Cameroon",
    "Canada",
    "Central African Republic",
    "Chad",
    "Chile",
    "China",
    "Colombia",
    "Comoros",
    "Republic of the Congo",
    "Democratic Republic of the Congo",
    "Costa Rica",
    "Croatia",
    "Cuba",
    "Cyprus",
    "Czech Republic",
    "Denmark",
    "Djibouti",
    "Dominica",
    "Dominican Republic",
    "Ecuador",
    "Egypt",
    "El Salvador",
    "Equatorial Guinea",
    "Eritrea",
    "Estonia",
    "Ethiopia",
    "Fiji",
    "Finland",
    "France",
    "Gabon",
    "Gambia",
    "Georgia",
    "Germany",
    "Ghana",
    "Greece",
    "Grenada",
    "Guatemala",
    "Guinea",
    "Guinea-Bissau",
    "Guyana",
    "Haiti",
    "Honduras",
    "Hungary",
    "Iceland",
    "India",
    "Indonesia",
    "Iran",
    "Iraq",
    "Ireland",
    "Israel",
    "Italy",
    "Jamaica",
    "Japan",
    "Jordan",
    "Kazakhstan",
    "Kenya",
    "Kiribati",
    "North Korea",
    "South Korea",
    "Kuwait",
    "Kyrgyzstan",
    "Laos",
    "Latvia",
    "Lebanon",
    "Lesotho",
    "Liberia",
    "Libya",
    "Liechtenstein",
    "Lithuania",
    "Luxembourg",
    "Macedonia",
    "Madagascar",
    "Malawi",
    "Malaysia",
    "Maldives",
    "Mali",
    "Malta",
    "Marshall Islands",
    "Mauritania",
    "Mauritius",
    "Mexico",
    "Micronesia",
    "Moldova",
    "Monaco",
    "Mongolia",
    "Montenegro",
    "Morocco",
    "Mozambique",
    "Myanmar",
    "Namibia",
    "Nauru",
    "Nepal",
    "Netherlands",
    "New Zealand",
    "Nicaragua",
    "Niger",
    "Nigeria",
    "Norway",
    "Oman",
    "Pakistan",
    "Palau",
    "Panama",
    "Papua New Guinea",
    "Paraguay",
    "Peru",
    "Philippines",
    "Poland",
    "Portugal",
    "Qatar",
    "Romania",
    "Russia",
    "Rwanda",
    "Saint Kitts and Nevis",
    "Saint Lucia",
    "Saint Vincent and the Grenadines",
    "Samoa",
    "San Marino",
    "São Tomé and Príncipe",
    "Saudi Arabia",
    "Senegal",
    "Serbia",
    "Seychelles",
    "Sierra Leone",
    "Singapore",
    "Slovakia",
    "Slovenia",
    "Solomon Islands",
    "Somalia",
    "South Africa",
    "South Sudan",
    "Spain",
    "Sri Lanka",
    "Sudan",
    "Suriname",
    "Swaziland",
    "Sweden",
    "Switzerland",
    "Syria",
    "Tajikistan",
    "Tanzania",
    "Thailand",
    "Timor-Leste",
    "Togo",
    "Tonga",
    "Trinidad and Tobago",
    "Tunisia",
    "Turkey",
    "Turkmenistan",
    "Tuvalu",
    "Uganda",
    "Ukraine",
    "United Arab Emirates",
    "United Kingdom",
    "United States",
    "Uruguay",
    "Uzbekistan",
    "Vanuatu",
    "Vatican City",
    "Venezuela",
    "Vietnam",
    "Yemen",
    "Zambia",
    "Zimbabwe"
  )

}