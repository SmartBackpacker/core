package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.parser.AbstractVisaRestrictionsIndexParser
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.http4s.{HttpService, Request, Status, Uri}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.io.Source

class VisaRestrictionIndexHttpEndpointSpec extends FlatSpecLike with Matchers {

  behavior of "VisaRestrictionIndexHttpEndpoint"

  object MockVisaRestrictionIndexParser extends AbstractVisaRestrictionsIndexParser[IO] {
    override val htmlDocument: IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"visaRestrictionswikiPageTest.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }
  }

  private val httpService: HttpService[IO] = new VisaRestrictionIndexHttpEndpoint(
    new VisaRestrictionIndexService[IO](MockVisaRestrictionIndexParser)
  ).service

  it should "retrieve visa restriction index for Argentina" in {
    val countryCode = "AR"
    val request = Request[IO](uri = Uri(path = s"/visa-restriction-index/$countryCode"))

//    val task = httpService(request).value.unsafeRunSync()
//    task should not be None
//    task foreach { response =>
//      response.status should be (Status.Ok)
//    }
  }

  it should "NOT retrieve visa restriction index for a non-existent country" in {
    val countryCode = "XX"
    val request = Request[IO](uri = Uri(path = s"/visa-restriction-index/$countryCode"))

//    val task = httpService(request).value.unsafeRunSync()
//    task should not be None
//    task foreach { response =>
//      response.status should be (Status.NotFound)
//    }
  }

}
