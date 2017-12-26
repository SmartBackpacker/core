package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import cats.syntax.option._
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.http.Http4sUtils._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRequirementsRepository
import com.github.gvolpe.smartbackpacker.service.{AbstractExchangeRateService, DestinationInfoService, CurrencyExchangeDTO}
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
    ("AR", "KO", Status.NotFound, "Country not found", """{"code":"100","error":"Country not found KO"}""")
  )

  private val repo = new VisaRequirementsRepository[IO] {
    override def findVisaRequirements(from: CountryCode, to: CountryCode): IO[Option[VisaRequirementsData]] = IO {
      if (to.value == "KO") none[VisaRequirementsData]
      else
      VisaRequirementsData(
        from = Country("AR".as[CountryCode], "Argentina".as[CountryName]),
        to   = Country("GB".as[CountryCode], "United Kingdom".as[CountryName]),
        visaCategory = VisaNotRequired,
        description = "90 days within any 180 day period"
      ).some
    }
  }

  private lazy val sbConfig = new SBConfiguration[IO]

  private val rateService = new AbstractExchangeRateService[IO](sbConfig) {
    override protected def retrieveExchangeRate(uri: String): IO[CurrencyExchangeDTO] = IO {
      CurrencyExchangeDTO("EUR", "", Map("RON" -> 4.59))
    }
  }

  val httpService: HttpService[IO] =
    middleware(
      new DestinationInfoHttpEndpoint(
        new DestinationInfoService[IO](sbConfig, repo, rateService),
        new HttpErrorHandler[IO]
      ).service
    )

}
