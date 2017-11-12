package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.http.ResponseBodyUtils._
import com.github.gvolpe.smartbackpacker.persistence.InMemoryAirlineDao
import com.github.gvolpe.smartbackpacker.service.AirlineService
import org.http4s.{HttpService, Query, Request, Response, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class AirlinesHttpEndpointSpec extends FlatSpecLike with Matchers with AirlinesHttpEndpointFixture {

  behavior of "AirlinesHttpEndpoint"

  val httpService: HttpService[IO] =
    new AirlinesHttpEndpoint(
      new AirlineService[IO](new InMemoryAirlineDao[IO])
    ).service

  forAll(examples) { (airline, expectedStatus, expectedBody) =>
    it should s"find the airline $airline" in {
      val request = Request[IO](uri = Uri(path = s"/airlines", query = Query(("name", Some(airline)))))

      val task: Option[Response[IO]] = httpService(request).value.unsafeRunSync()
      task should not be None
      task.foreach { response =>
        response.status should be (expectedStatus)
        assert(response.body.asString.contains(expectedBody))
      }
    }
  }

}

trait AirlinesHttpEndpointFixture extends PropertyChecks {

  val examples = Table(
    ("airline", "expectedStatus", "expectedBody"),
    ("Aer Lingus", Status.Ok, "baggagePolicy"),
    ("Transavia", Status.Ok, "baggagePolicy"),
    ("Ryan Air", Status.BadRequest, "Airline not found")
  )

}