package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.parser.AbstractVisaRestrictionsIndexParser
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.http4s.{HttpService, Request, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

import scala.io.Source

class VisaRestrictionIndexHttpEndpointSpec extends FlatSpecLike with Matchers with VisaRestrictionIndexFixture {

  forAll(examples) { (countryCode, expectedStatus, httpService) =>
    it should s"try to retrieve visa restriction index for $countryCode" in {
      val request = Request[IO](uri = Uri(path = s"/visa-restriction-index/$countryCode"))

      val task = httpService(request).value.unsafeRunSync()
      task should not be None
      task foreach { response =>
        response.status should be (expectedStatus)
      }
    }
  }

}

trait VisaRestrictionIndexFixture extends PropertyChecks {

  object MockVisaRestrictionIndexParser extends AbstractVisaRestrictionsIndexParser[IO] {
    override val htmlDocument: IO[Document] = IO {
      val browser = JsoupBrowser()
      val fileContent = Source.fromResource(s"visaRestrictionsWikiPageTest.html").mkString
      browser.parseString(fileContent).asInstanceOf[Document]
    }
  }

  object FailedVisaRestrictionIndexParser extends AbstractVisaRestrictionsIndexParser[IO] {
    override val htmlDocument: IO[Document] = IO.raiseError(new Exception("test"))
  }

  private val goodHttpService: HttpService[IO] = new VisaRestrictionIndexHttpEndpoint(
    new VisaRestrictionIndexService[IO](MockVisaRestrictionIndexParser)
  ).service

  private val badHttpService: HttpService[IO] = new VisaRestrictionIndexHttpEndpoint(
    new VisaRestrictionIndexService[IO](FailedVisaRestrictionIndexParser)
  ).service

  val examples = Table(
    ("countryCode", "expectedStatus", "httpService"),
    ("AR", Status.Ok, goodHttpService),
    ("XX", Status.NotFound, goodHttpService),
    ("IE", Status.BadRequest, badHttpService)
  )

}