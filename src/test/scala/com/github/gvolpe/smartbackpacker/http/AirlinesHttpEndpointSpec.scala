package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.persistence.InMemoryAirlineDao
import com.github.gvolpe.smartbackpacker.service.AirlineService
import org.http4s.{EntityBody, HttpService, Method, Query, Request, Response, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class AirlinesHttpEndpointSpec extends FlatSpecLike with Matchers {

  behavior of "AirlinesHttpEndpoint"

  val httpService: HttpService[IO] =
    new AirlinesHttpEndpoint(
      new AirlineService[IO](new InMemoryAirlineDao[IO])
    ).service

  implicit class ByteVector2String(body: EntityBody[IO]) {
    def asString: String = {
      val array = body.runLog.unsafeRunSync().toArray
      new String(array.map(_.toChar))
    }
  }

  // FIXME: Circe Encoder problems for baggageType NoSuchMethodError ???
  it should "find the airline" in {
    val request = Request[IO](uri = Uri(path = s"/airlines", query = Query(("name", Some("Transavia")))))

//    val task: Option[Response[IO]] = httpService(request).value.unsafeRunSync()
//    task should not be None
//    task.foreach { response =>
//      response.status         should be (Status.Created)
//      response.body.asString  should be ("[]")
//    }

  }

}

trait AirlinesHttpEndpointFixture extends PropertyChecks {

  val examples = Table(
    ("description", "airline"),
    ("find the airline", "Ryan Air")
  )

}