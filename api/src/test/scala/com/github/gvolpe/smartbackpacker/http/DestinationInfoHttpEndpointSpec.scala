package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.http.ResponseBodyUtils._
import com.github.gvolpe.smartbackpacker.service.CountryService
import com.github.gvolpe.smartbackpacker.{TestExchangeRateService, TestWikiPageParser}
import org.http4s.{HttpService, Query, Request, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class DestinationInfoHttpEndpointSpec extends FlatSpecLike with Matchers with DestinationInfoHttpEndpointFixture {

  forAll(examples) { (from, to, expectedStatus, expectedCountry, expectedVisa) =>
    it should s"retrieve visa requirements from $from to $to" in IOAssertion {
      val request = Request[IO](uri = Uri(path = s"/$ApiVersion/traveling/$from/to/$to", query = Query(("baseCurrency", Some("EUR")))))

      httpService(request).value.map { task =>
        task.fold(fail("Empty response")){ response =>
          response.status should be (expectedStatus)

          val body = response.body.asString
          assert(body.contains(expectedCountry))
          assert(body.contains(expectedVisa))
        }
      }
    }
  }

}

trait DestinationInfoHttpEndpointFixture extends PropertyChecks {

  val examples = Table(
    ("from", "code", "expectedStatus","expectedCountry", "expectedVisa"),
    ("AR", "GB", Status.Ok, "United Kingdom", "VisaNotRequired"),
    ("CA", "SO", Status.Ok, "Somalia", "VisaRequired"),
    ("AR", "KO", Status.BadRequest, "Country code not found", "")
  )

  val httpService: HttpService[IO] = new DestinationInfoHttpEndpoint(
    new CountryService[IO](TestWikiPageParser, TestExchangeRateService)
  ).service

}