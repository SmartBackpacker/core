package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.http.Http4sUtils._
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.CountryRepository
import com.github.gvolpe.smartbackpacker.service.CountryService
import org.http4s.{HttpService, Query, Request, Status, Uri}
import org.scalatest.{FlatSpecLike, Matchers}

class CountriesHttpEndpointSpec extends FlatSpecLike with Matchers with CountriesHttpEndpointFixture {

  it should s"find all the countries" in IOAssertion {
    val request = Request[IO](uri = Uri(path = s"/$ApiVersion/countries"))

    httpService(request).value.map { task =>
      task.fold(fail("Empty response")){ response =>
        response.status should be (Status.Ok)
        assert(response.body.asString.contains("Argentina"))
      }
    }
  }

  it should s"find all the schengen countries" in IOAssertion {
    val request = Request[IO](uri = Uri(path = s"/$ApiVersion/countries", query = Query(("query", Some("schengen")))))

    httpService(request).value.map { task =>
      task.fold(fail("Empty response")){ response =>
        response.status should be (Status.Ok)
        assert(response.body.asString.contains("Poland"))
      }
    }
  }

}

trait CountriesHttpEndpointFixture {

  private val testCountries = List(
    Country("AR".as[CountryCode], "Argentina".as[CountryName], "ARS".as[Currency])
  )

  private val testSchengenCountries = List(
    Country("PL".as[CountryCode], "Poland".as[CountryName], "PLN".as[Currency])
  )

  private val repo = new CountryRepository[IO] {
    override def findAll: IO[List[Country]] = IO(testCountries)
    override def findSchengen: IO[List[Country]] = IO(testSchengenCountries)
  }

  val httpService: HttpService[IO] =
    middleware(
      new CountriesHttpEndpoint[IO](
        new CountryService[IO](repo),
        new HttpErrorHandler[IO]
      ).service
    )

}
