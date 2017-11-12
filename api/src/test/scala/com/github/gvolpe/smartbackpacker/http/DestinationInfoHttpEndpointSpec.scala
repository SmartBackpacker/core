package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.{TestExchangeRateService, TestWikiPageParser}
import com.github.gvolpe.smartbackpacker.service.CountryService
import org.http4s.{HttpService, Query, Request, Uri}
import org.scalatest.{FlatSpecLike, Matchers}

class DestinationInfoHttpEndpointSpec extends FlatSpecLike with Matchers {

  behavior of "DestinationInfoHttpEndpoint"

  val httpService: HttpService[IO] = new DestinationInfoHttpEndpoint(
    new CountryService[IO](TestWikiPageParser, TestExchangeRateService)
  ).service

  it should "retrieve visa requirements for" in {
    val from  = "AR"
    val to    = "UK"
    val request = Request[IO](uri = Uri(path = s"/traveling/$from/to/$to", query = Query(("baseCurrency", Some("EUR")))))

//    val task = httpService(request).value.unsafeRunSync()
//    task should not be None
  }

}
